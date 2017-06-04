package com.educem.ruben.domo_meteo

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toolbar
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.educem.ruben.domo_meteo.CLASES.Error
import com.educem.ruben.domo_meteo.CLASES.User
import com.educem.ruben.domo_meteo.CLASES.UserK
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap

class LoginActivity : AppCompatActivity() {

    private var inputUsername : EditText? = null
    private var inputPassword : EditText? = null
    private var btLogin : Button? = null
    private var btToRegister : Button? = null
    private var pDialog : ProgressDialog? = null
    private var drawerLayout : DrawerLayout? = null
    private var LOGIN_URL : String = "http://192.168.1.36/API/Android/login_android.php"
    private var queue : RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // CASTING DE VIEWS A OBJETOS KOTLIN
        inputUsername = findViewById(R.id.loginUsername) as EditText
        inputPassword = findViewById(R.id.loginPassword) as EditText
        btLogin = findViewById(R.id.btLogin) as Button
        btToRegister = findViewById(R.id.btnLinkToRegisterScreen) as Button
        drawerLayout = findViewById(R.id.drawer_layout_login) as DrawerLayout

        // Creamos la cola para las peticiones de la libreria Volley de google 'this' is the Context
        queue = Volley.newRequestQueue(this)

        // Dialogo de progreso
        pDialog = ProgressDialog(this)
        pDialog?.setCancelable(false)

        // Evento click del botón para ir a registrarse
        btToRegister?.setOnClickListener {
            startActivity(Intent(applicationContext, RegistrarseActivity::class.java))
        }

        btLogin?.setOnClickListener {

            // Escondemos el teclado
            hideKeyboard()

            // Obtenemos los datos del formulario
            var user = inputUsername?.text.toString().trim()
            var pass = inputPassword?.text.toString().trim()

            // Comprobamos qe haya datos introducidos en ambas variables
            if(!user.isEmpty() && !pass.isEmpty())
            {
                try
                {
                    checkLogin(user, pass)
                }
                catch (excp : Exception)
                {
                    excp.printStackTrace()
                }
            }
            else
            {
                // Lanzamos mensaje al usuario
                lanzarSnack(R.id.drawer_layout_login, getString(R.string.errorFaltanDatos), Snackbar.LENGTH_LONG)
            }
        }
    }

    private fun checkLogin(user: String, pass: String)
    {
        // tag para cancelar la petición http por la libreria volley
        val tag_string_req = "req_login"

        // Mostramos el dialogo
        pDialog?.setMessage(getString(R.string.iniciandoLogin))
        pDialog?.show()

        // Creamos la peticion con HTTP con la libreria Volley de Google
        val myReq = object : StringRequest(Request.Method.POST,
                LOGIN_URL,
                requestSuccess(),
                requestError()) {

            @Throws(com.android.volley.AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("username", user)
                params.put("password", pass)
                return params
            }
        }

        // Añadimos la petición HTTP a la cola para que se ejecute
        queue?.add(myReq)
    }

    private fun  requestSuccess(): Response.Listener<String>? {
        return Response.Listener { response ->

            Log.d("Volley", response.toString())

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

                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
                else
                {
                    // Mostramos el error por pantalla al usuario
                    var mensajeError = obtenerError(error.numero)

                    lanzarSnack(R.id.drawer_layout_login, mensajeError, Snackbar.LENGTH_LONG)

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

    private fun requestError(): Response.ErrorListener {
        return Response.ErrorListener { error ->

            // Mostramos el error y quitamos el dialogo
            Log.d("Volley", error.toString())
            pDialog?.hide()
            lanzarSnack(R.id.drawer_layout_login, getString(R.string.noHayConexionServidor), Snackbar.LENGTH_LONG)
        }
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
            1 -> mensaje = getString(R.string.userNotEnabled)
            2 -> mensaje = getString(R.string.incorrectPassword)
            3 -> mensaje = getString(R.string.usernameNotExist)
        }

        return mensaje
    }
}
