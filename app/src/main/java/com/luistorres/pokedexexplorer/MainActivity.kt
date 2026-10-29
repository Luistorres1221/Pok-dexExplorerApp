package com.luistorres.pokedexexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.luistorres.pokedexexplorer.presentation.PokedexApp
import com.luistorres.pokedexexplorer.presentation.theme.PokedexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PokedexApp()
                }
            }
        }
    }
}
