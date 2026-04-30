package com.example.exerciseprotocols

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.exerciseprotocols.data.AppDatabase
import com.example.exerciseprotocols.data.ProtocolRepository
import com.example.exerciseprotocols.ui.screens.ProtocolBuilderScreen
import com.example.exerciseprotocols.ui.screens.ProtocolListScreen
import com.example.exerciseprotocols.ui.screens.ProtocolPlayerScreen
import com.example.exerciseprotocols.ui.theme.ExerciseProtocolsTheme
import com.example.exerciseprotocols.viewmodel.BuilderViewModel
import com.example.exerciseprotocols.viewmodel.BuilderViewModelFactory
import com.example.exerciseprotocols.viewmodel.ListViewModel
import com.example.exerciseprotocols.viewmodel.ListViewModelFactory
import com.example.exerciseprotocols.viewmodel.PlayerViewModel
import com.example.exerciseprotocols.viewmodel.PlayerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "exercise_protocols.db").build()
        val repository = ProtocolRepository(db.protocolDao())
        val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        setContent {
            ExerciseProtocolsTheme {
                val nav = rememberNavController()
                Scaffold { padding ->
                    NavHost(navController = nav, startDestination = "list", modifier = Modifier.padding(padding)) {
                        composable("list") {
                            val vm: ListViewModel = viewModel(factory = ListViewModelFactory(repository))
                            ProtocolListScreen(
                                viewModel = vm,
                                onAdd = { nav.navigate("builder/new") },
                                onOpen = { nav.navigate("player/$it") },
                                onEdit = { nav.navigate("builder/$it") }
                            )
                        }
                        composable("builder/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                            val raw = it.arguments?.getString("id") ?: "new"
                            val pid = raw.toLongOrNull()
                            val vm: BuilderViewModel = viewModel(factory = BuilderViewModelFactory(repository, pid))
                            ProtocolBuilderScreen(vm) { nav.popBackStack() }
                        }
                        composable("player/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                            val id = it.arguments!!.getLong("id")
                            val vm: PlayerViewModel = viewModel(factory = PlayerViewModelFactory(repository, id, tone))
                            ProtocolPlayerScreen(vm) { nav.popBackStack() }
                        }
                    }
                }
            }
        }
    }
}
