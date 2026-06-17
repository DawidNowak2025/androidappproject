package com.growgardentracker.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.growgardentracker.android.data.AppContainer
import com.growgardentracker.android.ui.navigation.AppNavigation
import com.growgardentracker.android.ui.theme.GrowGardenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(applicationContext)
        setContent {
            GrowGardenTheme {
                AppNavigation()
            }
        }
    }
}
