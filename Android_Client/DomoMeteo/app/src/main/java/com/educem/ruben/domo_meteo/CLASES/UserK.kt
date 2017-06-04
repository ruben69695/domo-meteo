package com.educem.ruben.domo_meteo.CLASES

import org.json.JSONException
import org.json.JSONObject

/**
 * Created by ruben on 4/06/17.
 */

/**
 * Constructor de la clase UserK
 * @param name Nombre de usuario
 * *
 * @param pass Contraseña del usuario
 * *
 * @param rol Rol del usuario, admin o standard
 * *
 * @param status 0 en el caso de desactivado, 1 en el caso de activado
 */
class UserK (var username : String, var password: String, var rol: String, var status: String) {

    // Secondary constructor
    constructor() : this("", "", "", "")

    //region FUNCTIONS AND METHODS

    @Throws(JSONException::class)
    fun convertJson_toUser(userJSON: JSONObject) {

        // A partir del JSON rellenamos las propiedades del objeto JAVA
        username = userJSON.getString("username")
        password = userJSON.getString("password")
        rol = userJSON.getString("role")
        status = userJSON.getString("enabled")
    }

    //endregion
}