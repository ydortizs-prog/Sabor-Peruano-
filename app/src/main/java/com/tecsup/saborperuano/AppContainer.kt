package com.tecsup.saborperuano

import com.tecsup.saborperuano.data.remote.RetrofitClient
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.data.repository.ItemRepositoryImpl

class AppContainer {
    val itemRepository: ItemRepository = ItemRepositoryImpl(RetrofitClient.api)
}
