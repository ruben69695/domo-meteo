package com.educem.ruben.domo_meteo

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap
import com.educem.ruben.domo_meteo.CLASES.Error
import com.educem.ruben.domo_meteo.CLASES.User
import com.educem.ruben.domo_meteo.CLASES.UserK

class RegistrarseActivity : AppCompatActivity() {

    private var username : EditText? = null
    private var password : EditText? = null
    private var confirmPassaword : EditText? = null
    private var URL_REGISTER: String = "http://192.168.1.36/API/Android/registro_android.php"
    private var queue : RequestQueue? = null
    private var pDialog : ProgressDialog? = null
    private var btRegistrarse : Button? = null
    private var btLinkToLogin : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrarse)

        // CASTING DE VIEWS A OBJETOS KOTLIN
        username = findViewById(R.id.register_username) as EditText
        password = findViewById(R.id.register_password) as EditText
        confirmPassaword = findViewById(R.id.register_ConfirmPassword) as EditText
        btRegistrarse = findViewById(R.id.btRegistrarse) as Button
        btLinkToLogin = findViewById(R.id.btnLinkToLogIn) as Button

        // Creamos la colla para las peticiones HTTP de la libreria Volley
        queue = Volley.newRequestQueue(this)

        // Progress Dialog
        pDialog = ProgressDialog(this)
        pDialog?.setCancelable(false)

        // Añadimos una escucha del boton registrarse
        btRegistrarse?.setOnClickListener {

            // Escondemos el teclado
            hideKeyboard()

            //Recogemos los datos introducidos por le usuario
            val user : String = username?.text.toString()
            val pass : String = password?.text.toString()
            val cPass : String = confirmPassaword?.text.toString()

            if(!user.isEmpty() && !pass.isEmpty() && !cPass.isEmpty())
            {
                // Comprobamos que las contraseñas coincidan
                if(pass.equals(cPass))
                {
                    try
                    {
                        // Intenamos hacer el registro
                        attemptToRegister(user, pass, cPass)
                    }
                    catch (excp : Exception)
                    {
                        excp.printStackTrace()
                    }
                }
                else
                {
                    // Lanzamos error de que las contraseñas no coinciden
                    lanzarSnack(R.id.drawer_layout_register, getString(R.string.passwordNoCoincide), Snackbar.LENGTH_LONG)
                }
            }
            else
            {
                lanzarSnack(R.id.drawer_layout_register, getString(R.string.errorFaltanDatos), Snackbar.LENGTH_LONG)
            }

        }

        // Añadimos una escucha al boton de volver al login
        btLinkToLogin?.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
        }
    }

    /**
     * Metodo para hacer el registro contra la base de datos
     */
    private fun attemptToRegister(username : String, password : String, cPassword : String)
    {
        // Tag para cancelar la petición
        val tag_string_req : String = "req_login"

        // Mostramos el dialogo de carga con un mensaje
        pDialog?.setMessage(getString(R.string.mensajeCargaRegistro))
        pDialog?.show()

        // Creamos la peticion HTTP con la libreria Volley de Google
        val myReq = object : StringRequest(Method.POST,
                URL_REGISTER,
                requestSuccess(),
                requestError()) {

            @Throws(com.android.volley.AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("username", username)
                params.put("password", password)
                params.put("confirmPassword", cPassword)
                return params
            }
        }

        // Añadimos la peticion a la cola para que se ejecute
        queue?.add(myReq)

    }

    private fun requestError(): Response.ErrorListener {
        return Response.ErrorListener { error ->

            // Mostramos el error y quitamos el dialogo
            Log.d("Volley", error.toString())
            pDialog?.hide()
            lanzarSnack(R.id.drawer_layout_register, getString(R.string.noHayConexionServidor), Snackbar.LENGTH_LONG)
        }
    }

    private fun  requestSuccess(): Response.Listener<String>? {
        return Response.Listener { response ->

            try
            {
                // Obtenemos la respuesta que es un JSON Array
                val jsonArray : JSONArray = JSONArray(response)

                // De esa array obtenemos el unico Objeto que hay
                val jsonObject : JSONObject = jsonArray.getJSONObject(0)


                // Creamos un objeto Error
                val error : Error = Error(jsonObject.getBoolean("error"), jsonObject.getInt("num"), jsonObject.getString("descr"))

                // Comprobamos que no haya error
                if(!error.error)
                {
                    // Creamos un objeto User
                    val user : UserK = UserK()

                    // Rellenamos los datos de la clase User a partir de un objeto JSON
                    user.convertJson_toUser(jsonObject.getJSONObject("user"))

                    // Mostramos al usuario que todo ha ido correcto
                    lanzarSnack(R.id.drawer_layout_register, getString(R.string.RegistroOK), Snackbar.LENGTH_LONG)

                    //Limpiamos el fomrulario de registro
                    limpiarFormulario()
                }
                else
                {
                    // Mostramos el error por pantalla al usuario
                    var mensajeError = obtenerError(error.numero)

                    lanzarSnack(R.id.drawer_layout_register, mensajeError, Snackbar.LENGTH_LONG)

                }

            }
            catch (excp : Exception)
            {
                excp.printStackTrace()
            }

            // Quitamos el dialogo de progreso
            pDialog?.hide()
        }
    }

    /**
     * Metodo para limpiar el formulario de registro
     */
    private fun limpiarFormulario()
    {
        username?.setText("")
        password?.setText("")
        confirmPassaword?.setText("")
    }

    /**
     * Metodo para esoncder el teclado cuando no lo necesites
     */
    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * Lanzar mensaje en forma de SnackBar
     * @param layoutID : identificador del DrawerLayout
     * *
     * @param message : mensaje que mostrar
     * *
     * @param duration : duracion del SnackBar
     */
    private fun lanzarSnack(layoutID: Int, message: String, duration: Int) {
        Snackbar.make(findViewById(layoutID)!!, message, duration).show()
    }

    /**
     * A partir de un numero de error obtenemos el mensaje
     * @param numError
     * *
     * @return
     */
    private fun obtenerError(numError: Int): String {

        var mensaje = ""

        when (numError) {
            1 -> mensaje = getString(R.string.errorAlRegistrar)
            2 -> mensaje = getString(R.string.passwordNoCoincide)
            3 -> mensaje = getString(R.string.errorUsuarioYaExiste)
        }

        return mensaje
    }
}