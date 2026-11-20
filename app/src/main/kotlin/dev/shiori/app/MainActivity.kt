package dev.shiori.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import dev.shiori.feature.library.LibraryScreen
import dev.shiori.feature.library.WorkDetailsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colors) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "library",
                ) {
                    composable("library") {
                        LibraryScreen(
                            onOpenDetails = { entryId ->
                                navController.navigate("work/$entryId")
                            },
                        )
                    }
                    composable(
                        route = "work/{entryId}",
                        arguments = listOf(navArgument("entryId") { type = NavType.StringType }),
                    ) {
                        WorkDetailsScreen(
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
