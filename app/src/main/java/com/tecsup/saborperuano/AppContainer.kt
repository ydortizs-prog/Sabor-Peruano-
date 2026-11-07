package com.tecsup.saborperuano

import android.content.Context
import androidx.room.Room
import com.tecsup.saborperuano.data.local.AppDatabase
import com.tecsup.saborperuano.data.remote.RetrofitClient
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.data.repository.ItemRepositoryImpl

class AppContainer private constructor(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "sabor-peruano.db"
    ).build()

    val itemRepository: ItemRepository = ItemRepositoryImpl(RetrofitClient.api, database)

    companion object {
        @Volatile
        private var instancia: AppContainer? = null

        fun obtener(context: Context): AppContainer = instancia ?: synchronized(this) {
            instancia ?: AppContainer(context.applicationContext).also { instancia = it }
        }
    }
}
