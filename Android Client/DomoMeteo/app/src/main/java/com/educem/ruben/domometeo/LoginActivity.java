package com.educem.ruben.domometeo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.educem.ruben.domometeo.CLASES.Error;
import com.educem.ruben.domometeo.CLASES.User;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText inputUsername, inputPassword;
    private Button btLogin, btToRegister;
    private ProgressDialog pDialog;
    private DrawerLayout drawerLayout;
    private String LOGIN_URL = "http://192.168.1.39/API/Android/login_android.php";
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // CASTING DE VIEWS A OBJETOS JAVA
        this.toolbar = (Toolbar)findViewById(R.id.login_toolbar);
        this.inputUsername = (EditText)findViewById(R.id.loginUsername);
        this.inputPassword = (EditText)findViewById(R.id.loginPassword);
        this.btLogin = (Button)findViewById(R.id.btLogin);
        this.drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout_login);
        this.btToRegister = (Button)findViewById(R.id.btnLinkToRegisterScreen);

        // Creamos una cola para las peticiones de la libreria Volley y le asignamos la clase actual 'this' is the Context
        queue = Volley.newRequestQueue(this);

        // Progress Dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Evento click del boton para ir a registrarse
        btToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegistrarseActivity.class));
            }
        });

        // Evento click del boton de login
        btLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Escondemos el teclado
                hideKeyboard();

                // Get the data from the Input
                String user = inputUsername.getText().toString().trim();
                String pass = inputPassword.getText().toString().trim();

                // Comprobamos que haya datos introducidos en ambos textboxes
                if(!user.isEmpty() && !pass.isEmpty())
                {
                    // Comprobar datos contra la base de datos externa
                    try {
                        checkLogin(user, pass);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    // Lanzamos un mensaje al usuario notificandole que faltan datos por introducir
                    Snackbar.make(findViewById(R.id.drawer_layout_login), getString(R.string.errorFaltanDatos), Snackbar.LENGTH_LONG).show();
                }
            }
        });


    }

    /**
     * Function to verify login details in database
     * @param username nombre de usuario
     * @param password contraseña del usuario
     */
    private void checkLogin(final String username, final String password) throws JSONException {
        // Tag para cancelar la petición
        String tag_string_req = "req_login";

        // Mostramos dialogo
        pDialog.setMessage(getString(R.string.iniciandoLogin));
        pDialog.show();

        // Creamos la peticions HTTP con la libreria Volley de Google
        StringRequest myReq = new StringRequest(Request.Method.POST,
                LOGIN_URL,
                requestSuccess(),
                requestError()) {

            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        queue.add(myReq);

    }

    private Response.ErrorListener requestError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
                pDialog.hide();
            }
        };
    }

    private Response.Listener<String> requestSuccess() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    Log.d("Volley", response.toString());

                    // Obtenemos la Array que nos retorna
                    JSONArray jsonResponse = new JSONArray(response.toString());

                    // De esa Array Obtenemos el unico objeto que hay con indice 0
                    JSONObject jsonObjectResponse = jsonResponse.getJSONObject(0);

                    // Creamos un obeto error
                    Error error = new Error(jsonObjectResponse.getBoolean("error"), jsonObjectResponse.getInt("num"), jsonObjectResponse.getString("descr"));

                    // Si no hay error en el login obtenemos la información completa del usuario a traves del JSON
                    if(!error.getError())
                    {
                        // Creamos un objeto usuario
                        User usuario = new User();

                        // Rellenamos los datos de la clase User a partir de un objeto JSON
                        usuario.convertJson_toUser(jsonObjectResponse.getJSONObject("user"));

                        // Una vez el inicio es correcto arrancamos el MainActivity y entramos en la Aplicación
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    }
                    else
                    {
                        String mensajeError = obtenerError(error.getNumero());
                        lanzarSnack(R.id.drawer_layout_login, mensajeError, Snackbar.LENGTH_LONG);
                    }


                    pDialog.hide();
                }
                catch (Exception excp)
                {
                    excp.printStackTrace();
                }
            }
        };
    }

    /**
     * A partir de un numero de error obtenemos el mensaje
     * @param numError
     * @return
     */
    private String obtenerError(int numError) {

        String mensaje = "";

        switch (numError)
        {
            case 1:
                mensaje = getString(R.string.userNotEnabled);
                break;
            case 2:
                mensaje = getString(R.string.incorrectPassword);
                break;
            case 3:
                mensaje = getString(R.string.usernameNotExist);
                break;
        }

        return mensaje;
    }

    /**
     * Lanzar mensaje en forma de SnackBar
     * @param layoutID : identificador del DrawerLayout
     * @param message : mensaje que mostrar
     * @param duration : duracion del SnackBar
     */
    private void lanzarSnack(int layoutID, String message, int duration)
    {
        Snackbar.make(findViewById(layoutID), message, duration).show();
    }

    /**
     * Metodo para esoncder el teclado cuando no lo necesites
     */
    private void hideKeyboard()
    {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}
