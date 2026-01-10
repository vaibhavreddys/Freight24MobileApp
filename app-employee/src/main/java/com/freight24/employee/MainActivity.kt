package com.freight24.employee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// import com.freight24.ui.theme.Freight24Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
//            Freight24Theme {
//                //LoginScreen()
//            }
        }
    }
}
