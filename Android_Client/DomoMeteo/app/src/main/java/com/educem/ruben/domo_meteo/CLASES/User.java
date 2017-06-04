package com.educem.ruben.domo_meteo.CLASES;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ruben on 3/06/17.
 */

public class User {

    private String username;
    private String password;
    private String rol;
    private String status;

    /**
     * Constructor de la clase UserK
     * @param name Nombre de usuario
     * @param pass Contraseña del usuario
     * @param rol Rol del usuario, admin o standard
     * @param status 0 en el caso de desactivado, 1 en el caso de activado
     */
    public User(String name, String pass, String rol, String status)
    {
        this.username = name;
        this.password = pass;
        this.rol = rol;
        this.status = status;
    }

    public User()
    {

    }

    //region FUNCTIONS AND METHODS

    public void convertJson_toUser(JSONObject userJSON) throws JSONException {

        // A partir del JSON rellenamos las propiedades del objeto JAVA
        this.username = userJSON.getString("username");
        this.password = userJSON.getString("password");
        this.rol = userJSON.getString("role");
        this.status = userJSON.getString("enabled");
    }

    //endregion

    //region SETTERS DE LA CLASE

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    //endregion

    // region GETTERS DE LA CLASE
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRol() {
        return rol;
    }

    public String getStatus() {
        return status;
    }

    //endregion
}
