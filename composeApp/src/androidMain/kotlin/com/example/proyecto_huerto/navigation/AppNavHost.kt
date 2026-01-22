package com.example.proyecto_huerto.navigation
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleCoroutineScope
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
import com.example.proyecto_huerto.profile.ProfileScreen
import com.example.proyecto_huerto.screens.*
import com.example.proyecto_huerto.viewmodel.HuertoUiState
import com.example.proyecto_huerto.viewmodel.HuertoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppNavHost(
    googleAuthUiClient: GoogleAuthUiClient,
    lifecycleScope: LifecycleCoroutineScope,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val navController = rememberNavController()
    val bancalViewModel: BancalViewModel = viewModel()
    val diarioViewModel: DiarioViewModel = viewModel()
    val huertoViewModel: HuertoViewModel = viewModel()

    NavHost(navController = navController, startDestination = "sign_in") {
        composable("sign_in") {
            val signInViewModel = viewModel<SignInViewModel>()
            val state by signInViewModel.state.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(key1 = Unit) {
                if (googleAuthUiClient.getSignedInUser() != null) {
                    navController.navigate("home") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                }
            }

            val launcher =
                rememberLauncherForActivityResult(
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
                    Toast.makeText(context, "Inicio de sesión correcto", Toast.LENGTH_LONG).show()
                    navController.navigate("home") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                    signInViewModel.resetState()
                }
            }


            LaunchedEffect(key1 = state.signInError) {
                state.signInError?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
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
            val context = LocalContext.current
            ForgotPasswordScreen(
                onSendPasswordResetEmail = { email ->
                    lifecycleScope.launch {
                        googleAuthUiClient.sendPasswordResetEmail(email).onSuccess {
                            Toast.makeText(context, "Correo de recuperación enviado.", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }.onFailure {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
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

        composable("diario_cultivo") {
            DiarioScreen(
                viewModel = diarioViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("guia_hortalizas") {
            GuiaHortalizasScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { nombre -> navController.navigate("detalle_hortaliza/$nombre") },
                viewModel = huertoViewModel
            )
        }

        composable(
            route = "detalle_hortaliza/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre")
            DetalleHortalizaScreen(
                nombreHortaliza = nombre ?: "",
                onBack = { navController.popBackStack() },
                viewModel = huertoViewModel
            )
        }

        composable("profile") {
            ProfileScreen(
                userData = googleAuthUiClient.getSignedInUser(),
                onSignOut = {
                    lifecycleScope.launch {
                        googleAuthUiClient.signOut()
                        navController.navigate("sign_in") { popUpTo(0) }
                    }
                },
                onBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate("about") },
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        }

        composable("plagas") {
            PlagasScreen(
                onPlagaClick = { plagaId -> navController.navigate("plaga_detail/$plagaId") },
                onBack = { navController.popBackStack() },
                viewModel = huertoViewModel
            )
        }

        composable(
            route = "plaga_detail/{plagaId}",
            arguments = listOf(navArgument("plagaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val plagaId = backStackEntry.arguments?.getString("plagaId")
            PlagaDetailScreen(
                plagaId = plagaId ?: "",
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