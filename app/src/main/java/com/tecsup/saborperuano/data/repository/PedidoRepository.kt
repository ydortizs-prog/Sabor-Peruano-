package com.tecsup.saborperuano.data.repository

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Serializable
internal data class LineaSolicitud(val id: String, val cantidad: Int)

@Serializable
internal data class SolicitudPedido(
    val intento: String,
    val nombre: String,
    val telefono: String,
    val entrega: String,
    val direccion: String?,
    val observaciones: String,
    val lineas: List<LineaSolicitud>,
    val subtotalCentimos: Long
)

@Serializable
internal data class PedidoGuardado(val pedidoId: String, val totalCentimos: Long)

@Serializable
internal data class SolicitudCallable(val data: SolicitudPedido)

@Serializable
internal data class RespuestaCallable(val result: PedidoGuardado)

internal interface ApiPedidos {
    @POST("crearPedido")
    suspend fun crear(
        @Header("Authorization") autorizacion: String,
        @Body solicitud: SolicitudCallable
    ): RespuestaCallable
}

internal class PedidoRepository(val habilitado: Boolean) {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val api by lazy {
        val proyecto = requireNotNull(FirebaseApp.getInstance().options.projectId)
        Retrofit.Builder()
            .baseUrl("https://us-central1-$proyecto.cloudfunctions.net/")
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(ApiPedidos::class.java)
    }

    suspend fun guardar(uid: String, solicitud: SolicitudPedido): PedidoGuardado {
        check(habilitado)
        val usuario = checkNotNull(auth.currentUser)
        check(usuario.uid == uid)
        val token = checkNotNull(usuario.getIdToken(false).await().token)
        check(auth.currentUser?.uid == uid)
        return api.crear("Bearer $token", SolicitudCallable(solicitud)).result
    }
}
