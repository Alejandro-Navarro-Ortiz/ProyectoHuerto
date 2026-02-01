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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.example.proyecto_huerto.location.AndroidLocationService
import com.example.proyecto_huerto.notifications.NotificationScheduler
import com.example.proyecto_huerto.profile.ProfileScreen
import com.example.proyecto_huerto.profile.ProfileViewModel
import com.example.proyecto_huerto.screens.*
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import com.example.proyecto_huerto.viewmodel.HuertoViewModel
import com.example.proyecto_huerto.weather.WeatherRepository
import com.example.proyecto_huerto.weather.WeatherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppNavHost(
    googleAuthUiClient: GoogleAuthUiClient,
    lifecycleScope: CoroutineScope,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var showSplash by remember { mutableStateOf(true) }

    var currentLanguage by rememberSaveable {
        mutableStateOf(
            AppCompatDelegate.getApplicationLocales().get(0)?.language ?: Locale.getDefault().language
        )
    }

    val onLanguageChange: (String) -> Unit = { newLang ->
        if (currentLanguage != newLang) {
            currentLanguage = newLang
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
            showSplash = true // Re-activar splash al cambiar de idioma
        }
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else {
        val notificationScheduler = NotificationScheduler(context)

        val bancalViewModel: BancalViewModel = viewModel(
            factory = GenericViewModelFactory { BancalViewModel(notificationScheduler) }
        )
        val diarioViewModel: DiarioViewModel = viewModel<DiarioViewModel>()
        val huertoViewModel: HuertoViewModel = viewModel<HuertoViewModel>()

        val weatherViewModel: WeatherViewModel = viewModel(
            factory = GenericViewModelFactory {
                WeatherViewModel(
                    repository = WeatherRepository(),
                    locationService = AndroidLocationService(context)
                )
            }
        )

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val notificationsGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
            val locationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (!notificationsGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(context, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
            }
            if (locationGranted) {
                weatherViewModel.loadWeather()
            }
        }

        LaunchedEffect(Unit) {
            val permissionsToRequest = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            if (permissionsToRequest.isNotEmpty()) {
                permissionLauncher.launch(permissionsToRequest.toTypedArray())
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

                SignUpScreen(
                    onSignUpClick = { email, password ->
                        lifecycleScope.launch {
                            val result = googleAuthUiClient.signUpWithEmail(email, password)
                            signInViewModel.onSignInResult(result)
                        }
                    },
                    onSignInClick = {
                        lifecycleScope.launch {
                            val signInIntentSender = googleAuthUiClient.signIn()
                            launcher.launch(
                                IntentSenderRequest.Builder(signInIntentSender ?: return@launch).build()
                            )
                        }
                    },
                    onNavigateToSignIn = { navController.popBackStack() }
                )
            }

            composable("home") {
                val actividades by diarioViewModel.actividades.collectAsState()
                val weatherState by weatherViewModel.weatherState.collectAsState()
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                HomeScreen(
                    weatherState = weatherState,
                    recentActivities = actividades.map {
                        val formattedDate = sdf.format(Date(it.fecha))
                        "${it.tipo} en ${it.nombreBancal}: ${it.detalle} - $formattedDate"
                    }.reversed(),
                    onNavigate = { route -> navController.navigate(route) },
                    onRefreshWeather = { weatherViewModel.loadWeather() }
                )
            }

            composable("gestion_bancales") {
                GestionBancalesScreen(
                    viewModel = bancalViewModel,
                    onNavigate = {
                        if (it == "Inicio") {
                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
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
                val bancalId = backStackEntry.arguments?.getString("bancalId")

                DetalleBancalScreen(
                    bancalState = HuertoUiState.Success(bancales),
                    hortalizasState = hortalizasState,
                    bancalId = bancalId,
                    currentLanguage = currentLanguage,
                    onBack = { navController.popBackStack() }, // Implementación de la navegación hacia atrás
                    onUpdateCultivos = { posiciones, nombreHortaliza ->
                        val bancal = bancalViewModel.getBancalById(bancalId ?: "")
                        val hortaliza = (hortalizasState as? HuertoUiState.Success)?.data?.find { it.nombre == nombreHortaliza }
                        if (bancal != null && hortaliza != null) {
                            bancalViewModel.updateCultivos(bancal, posiciones, hortaliza)
                        }
                    },
                    onRegarCultivos = { posiciones ->
                        val bancal = bancalViewModel.getBancalById(bancalId ?: "")
                        if (bancal != null) {
                            bancalViewModel.regarCultivos(bancal, posiciones)
                        }
                    },
                    onAbonarCultivos = { posiciones ->
                        val bancal = bancalViewModel.getBancalById(bancalId ?: "")
                        if (bancal != null) {
                            bancalViewModel.abonarCultivos(bancal, posiciones)
                        }
                    },
                    onCosecharCultivos = { posiciones ->
                        val bancal = bancalViewModel.getBancalById(bancalId ?: "")
                        if (bancal != null) {
                            bancalViewModel.cosecharCultivos(bancal, posiciones)
                        }
                    }
                )
            }

            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel<ProfileViewModel>()
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
                DiarioScreen(
                    viewModel = diarioViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("guia_hortalizas") {
                val hortalizasState by huertoViewModel.hortalizasState.collectAsState()
                GuiaHortalizasScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToDetail = { nombre -> navController.navigate("detalle_hortaliza/$nombre") },
                    uiState = hortalizasState
                )
            }

            composable(
                route = "detalle_hortaliza/{hortalizaId}",
                arguments = listOf(navArgument("hortalizaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val hortalizaId = backStackEntry.arguments?.getString("hortalizaId") ?: ""
                val hortalizasState by huertoViewModel.hortalizasState.collectAsState()

                DetalleHortalizaScreen(
                    hortalizaId = hortalizaId,
                    onBack = { navController.popBackStack() },
                    uiState = hortalizasState
                )
            }

            composable("plagas") {
                val plagasState by huertoViewModel.plagasState.collectAsState()
                PlagasScreen(
                    onPlagaClick = { plagaId -> navController.navigate("plaga_detalle/$plagaId") },
                    uiState = plagasState,
                    onBack = { navController.popBackStack() },
                    language = currentLanguage
                )
            }

            composable(
                route = "plaga_detalle/{plagaId}",
                arguments = listOf(navArgument("plagaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val plagaId = backStackEntry.arguments?.getString("plagaId")
                val plagasState by huertoViewModel.plagasState.collectAsState()

                if (plagaId != null) {
                    PlagaDetailScreen(
                        plagaId = plagaId,
                        uiState = plagasState,
                        onBack = { navController.popBackStack() },
                        language = currentLanguage
                    )
                }
            }

            composable("consejos") {
                ConsejosScreen(
                    onNavigate = { route ->
                        if (route == "home") {
                            navController.navigate(route) { popUpTo(route) { inclusive = true } }
                        }
                    }
                )
            }

            composable("about") {
                AboutScreen { navController.popBackStack() }
            }
        }
    }
}

class GenericViewModelFactory<T : ViewModel>(private val constructor: () -> T) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return constructor() as T
    }
}
