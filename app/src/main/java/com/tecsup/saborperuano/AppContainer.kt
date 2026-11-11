package com.tecsup.saborperuano

import android.content.Context
import androidx.room.Room
import com.tecsup.saborperuano.data.auth.AuthRepository
import com.tecsup.saborperuano.data.auth.FirebaseAuthRepository
import com.tecsup.saborperuano.data.local.AppDatabase
import com.tecsup.saborperuano.data.local.PreferenciasCliente
import com.tecsup.saborperuano.data.remote.RetrofitClient
import com.tecsup.saborperuano.data.repository.CarritoRepository
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.data.repository.ItemRepositoryImpl
import com.tecsup.saborperuano.data.traduccion.TraductorMlKit
import com.tecsup.saborperuano.data.repository.MenuLocalRepository
import com.tecsup.saborperuano.data.repository.PedidoRepository
import com.tecsup.saborperuano.notification.Notificador
import com.tecsup.saborperuano.notification.NotificadorAndroid
import kotlinx.serialization.json.Json

class AppContainer private constructor(private val context: Context) {
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "sabor-peruano.db"
    ).addMigrations(AppDatabase.MIGRACION_1_2, AppDatabase.MIGRACION_2_3).build()

    private val preciosDePrueba: Map<String, Int> =
        if ("precios-prueba.json" in context.assets.list("").orEmpty()) {
            context.assets.open("precios-prueba.json").bufferedReader().use { archivo ->
                Json.decodeFromString<Map<String, Int>>(archivo.readText())
            }
        } else {
            emptyMap()
        }

    val itemRepository: ItemRepository =
        ItemRepositoryImpl(RetrofitClient.api, database, preciosDePrueba, TraductorMlKit())

    internal val menuLocal = MenuLocalRepository(database)

    fun crearCarritoDeSesion(): CarritoRepository = CarritoRepository()

    internal fun preferenciasCliente(uid: String): PreferenciasCliente =
        PreferenciasCliente(context.getSharedPreferences("cliente_$uid", Context.MODE_PRIVATE))

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }

    internal val pedidoRepository = PedidoRepository(
        context.resources.getBoolean(R.bool.pedido_backend_habilitado)
    )
    val notificador: Notificador = NotificadorAndroid(context)

    companion object {
        @Volatile
        private var instancia: AppContainer? = null

        fun obtener(context: Context): AppContainer = instancia ?: synchronized(this) {
            instancia ?: AppContainer(context.applicationContext).also { instancia = it }
        }
    }
}
