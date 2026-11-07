package com.tecsup.saborperuano

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.tecsup.saborperuano.ui.lista.ListaScreen
import com.tecsup.saborperuano.ui.lista.ListaViewModel
import com.tecsup.saborperuano.ui.theme.SaborPeruanoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(this, ListaViewModel.Factory(AppContainer()))[
            ListaViewModel::class.java
        ]

        setContent {
            SaborPeruanoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ListaScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
