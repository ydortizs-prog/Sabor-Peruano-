package com.tecsup.saborperuano.data.auth

import com.tecsup.saborperuano.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion>
    suspend fun registrar(nombre: String, correo: String, clave: String): Sesion
    suspend fun enviarRestablecimiento(correo: String)
    suspend fun sesionActual(): Sesion?
    suspend fun confirmarAdministrador(uid: String): Boolean
    fun cerrarSesion()
}
