package com.example.proyecto_huerto.navigation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto_huerto.auth.ForgotPasswordScreen
import com.example.proyecto_huerto.auth.GoogleAuthUiClient
import com.example.proyecto_huerto.auth.SignInScreen
import com.example.proyecto_huerto.auth.SignInViewModel
import com.example.proyecto_huerto.auth.SignUpScreen
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.notifications.NotificationScheduler
import com.example.proyecto_huerto.profile.ProfileScreen
import com.example.proyecto_huerto.profile.ProfileViewModel
import com.example.proyecto_huerto.screens.*
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import com.example.proyecto_huerto.viewmodel.HuertoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Componente principal de navegación de la aplicación para Android.
 * Gestiona rutas, inyección de dependencias, permisos y preferencias de idioma.
 */
@Composable
fun AppNavHost(
    googleAuthUiClient: GoogleAuthUiClient,
    lifecycleScope: CoroutineScope,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ESTADO DE IDIOMA PERSISTENTE: Usamos rememberSaveable para que el valor sobreviva
    // al REINICIO de la Activity que provoca el cambio de idioma en Android.
    var currentLanguage by rememberSaveable {
        mutableStateOf(
            AppCompatDelegate.getApplicationLocales().get(0)?.language ?: Locale.getDefault().language
        )
    }

    /**
     * Cambia el idioma de la aplicación utilizando AppCompatDelegate.
     * Al llamar a setApplicationLocales, Android refresca la configuración global.
     */
    val onLanguageChange: (String) -> Unit = { newLang ->
        if (currentLanguage != newLang) {
            currentLanguage = newLang
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
            // Nota: La app se reiniciará automáticamente gracias al cambio en el Manifiesto,
            // asegurando que todos los strings.xml se carguen de nuevo.
        }
    }

    val notificationScheduler = NotificationScheduler(context)

    val bancalViewModel: BancalViewModel = viewModel(
        factory = GenericViewModelFactory { BancalViewModel(notificationScheduler) }
    )

    val diarioViewModel: DiarioViewModel = viewModel()
    val huertoViewModel: HuertoViewModel = viewModel()

    // Gestión de permisos de notificaciones
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    NavHost(navController = navController, startDestination = "sign_in") {

        composable("sign_in") {
            val signInViewModel = viewModel<SignInViewModel>()
            val state by signInViewModel.state.collectAsState()

            LaunchedEffect(key1 = Unit) {
                if (googleAuthUiClient.getSignedInUser() != null) {
                    navController.navigate("home") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                }
            }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        lifecycleScope.launch {
                            val signInResult = googleAuthUiClient.signInWithIntent(
                                intent = result.data ?: return@launch
                            )
                            signInViewModel.onSignInResult(signInResult)
                        }
                    }
                }
            )

            LaunchedEffect(key1 = state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    navController.navigate("home") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                    signInViewModel.resetState()
                }
            }

            SignInScreen(
                onSignInClick = {
                    lifecycleScope.launch {
                        val signInIntentSender = googleAuthUiClient.signIn()
                        launcher.launch(
                            IntentSenderRequest.Builder(signInIntentSender ?: return@launch).build()
                        )
                    }
                },
                onLoginClick = { email, password ->
                    lifecycleScope.launch {
                        val result = googleAuthUiClient.signInWithEmail(email, password)
                        signInViewModel.onSignInResult(result)
                    }
                },
                onNavigateToSignUp = { navController.navigate("sign_up") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onSendPasswordResetEmail = { email ->
                    lifecycleScope.launch {
                        googleAuthUiClient.sendPasswordResetEmail(email).onSuccess {
                            navController.popBackStack()
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("sign_up") {
            val signInViewModel = viewModel<SignInViewModel>()
            val state by signInViewModel.state.collectAsState()

            LaunchedEffect(key1 = state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    navController.navigate("home") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                    signInViewModel.resetState()
                }
            }

            SignUpScreen(
                onSignUpClick = { email, password ->
                    lifecycleScope.launch {
                        val result = googleAuthUiClient.signUpWithEmail(email, password)
                        signInViewModel.onSignInResult(result)
                    }
                },
                onNavigateToSignIn = { navController.popBackStack() }
            )
        }

        composable("home") {
            val actividades by diarioViewModel.actividades.collectAsState()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            HomeScreen(
                recentActivities = actividades.map {
                    val formattedDate = sdf.format(Date(it.fecha))
                    "${it.tipo} en ${it.nombreBancal}: ${it.detalle} - $formattedDate"
                }.reversed(),
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable("gestion_bancales") {
            val bancales by bancalViewModel.bancales.collectAsState()
            GestionBancalesScreen(
                bancales = bancales,
                onAddBancal = { nombre, ancho, largo -> bancalViewModel.addBancal(nombre, ancho, largo) },
                onDeleteBancal = { bancalId -> bancalViewModel.deleteBancal(bancalId) },
                onNavigate = { screen ->
                    if (screen == "Perfil") navController.navigate("profile")
                    if (screen == "Inicio") navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onBancalClick = { id -> navController.navigate("detalle_bancal/$id") }
            )
        }

        composable(
            route = "detalle_bancal/{bancalId}",
            arguments = listOf(navArgument("bancalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bancales by bancalViewModel.bancales.collectAsState()
            val hortalizasState by huertoViewModel.hortalizasState.collectAsState()

            val id = backStackEntry.arguments?.getString("bancalId")
            val bancal = id?.let { bancalId -> bancales.find { it.id == bancalId } }

            if (bancal != null) {
                DetalleBancalScreen(
                    bancal = bancal,
                    onBack = { navController.popBackStack() },
                    onUpdateCultivos = { posiciones, nombreHortaliza ->
                        if (hortalizasState is HuertoUiState.Success) {
                            val hortaliza = (hortalizasState as HuertoUiState.Success<List<Hortaliza>>).data.find { it.nombre == nombreHortaliza }
                            if (hortaliza != null) {
                                bancalViewModel.updateCultivos(bancal, posiciones, hortaliza)
                            }
                        }
                    },
                    onRegarCultivos = { posiciones ->
                        bancalViewModel.regarCultivos(bancal, posiciones)
                    },
                    viewModel = huertoViewModel
                )
            }
        }

        composable("profile") {
            val profileViewModel: ProfileViewModel = viewModel()
            ProfileScreen(
                onSignOut = {
                    lifecycleScope.launch {
                        googleAuthUiClient.signOut()
                        navController.navigate("sign_in") { popUpTo(0) }
                    }
                },
                onBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate("about") },
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange,
                viewModel = profileViewModel
            )
        }

        composable("diario_cultivo") {
            DiarioScreen(viewModel = diarioViewModel, onBack = { navController.popBackStack() })
        }

        composable("guia_hortalizas") {
            GuiaHortalizasScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { nombre -> navController.navigate("detalle_hortaliza/$nombre") },
                viewModel = huertoViewModel
            )
        }

        composable("plagas") {
            PlagasScreen(
                onPlagaClick = { plagaId -> navController.navigate("plaga_detail/$plagaId") },
                onBack = { navController.popBackStack() },
                viewModel = huertoViewModel
            )
        }

        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable("consejos") {
            ConsejosScreen(onNavigate = { route -> navController.navigate(route) })
        }
    }
}

/**
 * Factory genérica para permitir la creación de ViewModels que requieren
 * parámetros en su constructor.
 */
class GenericViewModelFactory<T : androidx.lifecycle.ViewModel>(
    private val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}