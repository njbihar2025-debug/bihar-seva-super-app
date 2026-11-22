package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.authStateFlow
import com.example.data.NewsRepository
import com.example.data.SettingsDataStore
import com.example.data.SuperAppRepository
import com.example.data.UserSettings
import com.example.ui.SuperAppShell
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.BiharSevaTheme
import com.example.viewmodel.SuperAppViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiharSevaRoot()
        }
    }
}

@Composable
fun BiharSevaRoot() {
    val context = LocalContext.current
    val settingsStore = remember(context) { SettingsDataStore(context.applicationContext) }
    val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle(initialValue = UserSettings())
    val currentUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    BiharSevaTheme(themeMode = settings.themeMode) {
        val user = currentUser
        if (user == null) {
            AuthScreen(onAuthSuccess = {})
        } else {
            val uid = user.uid
            key(uid) {
                val superAppViewModel: SuperAppViewModel = viewModel(
                    key = uid,
                    factory = viewModelFactory {
                        initializer {
                            val app = checkNotNull(this[APPLICATION_KEY]) {
                                "APPLICATION_KEY missing from CreationExtras"
                            }
                            val databaseId = app.getString(R.string.firestore_database_id)
                            val db = FirebaseFirestore.getInstance(databaseId)
                            SuperAppViewModel(
                                repository = SuperAppRepository(db),
                                newsRepository = NewsRepository(),
                                settingsStore = SettingsDataStore(app.applicationContext),
                                currentUserId = uid
                            )
                        }
                    }
                )
                SuperAppShell(viewModel = superAppViewModel)
            }
        }
    }
}
