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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_huerto.auth.GoogleAuthUiClient
import com.example.proyecto_huerto.auth.SignInViewModel
import com.example.proyecto_huerto.auth.SignInScreen
import com.example.proyecto_huerto.auth.SignUpScreen
import com.example.proyecto_huerto.profile.ProfileScreen
import com.example.proyecto_huerto.screens.GestionBancalesScreen
import com.example.proyecto_huerto.screens.BancalViewModel
import com.example.proyecto_huerto.screens.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    googleAuthUiClient: GoogleAuthUiClient,
    lifecycleScope: LifecycleCoroutineScope
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "sign_in") {
        composable("sign_in") {
            val signInViewModel = viewModel<SignInViewModel>()
            val state by signInViewModel.state.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(key1 = Unit) {
                if(googleAuthUiClient.getSignedInUser() != null) {
                    navController.navigate("home") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                }
            }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = {
                    result ->
                    if(result.resultCode == Activity.RESULT_OK) {
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
                if(state.isSignInSuccessful) {
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
                    signInViewModel.clearError()
                }
            }

            SignInScreen(
                state = state,
                onSignInClick = {
                    lifecycleScope.launch {
                        val signInIntentSender = googleAuthUiClient.signIn()
                        launcher.launch(
                            IntentSenderRequest.Builder(
                                signInIntentSender ?: return@launch
                            ).build()
                        )
                    }
                },
                onLoginClick = { email, password ->
                    lifecycleScope.launch {
                        val result = googleAuthUiClient.signInWithEmail(email, password)
                        signInViewModel.onSignInResult(result)
                    }
                },
                onNavigateToSignUp = { navController.navigate("sign_up") }
            )
        }
        
        composable("sign_up") {
            val signInViewModel = viewModel<SignInViewModel>()
            val state by signInViewModel.state.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(key1 = state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    Toast.makeText(context, "Registro completado", Toast.LENGTH_LONG).show()
                    navController.navigate("home") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                    signInViewModel.resetState()
                }
            }

            LaunchedEffect(key1 = state.signInError) {
                state.signInError?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    signInViewModel.clearError()
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
            HomeScreen { route ->
                navController.navigate(route)
            }
        }

        composable("gestion_bancales") {
            val bancalViewModel = viewModel<BancalViewModel>()
            val bancales by bancalViewModel.bancales.collectAsState()
            
            GestionBancalesScreen(
                bancales = bancales,
                onAddBancal = { nombre -> bancalViewModel.addBancal(nombre) },
                onNavigate = { screen ->
                    if (screen == "Perfil") navController.navigate("profile")
                    if (screen == "Inicio") navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("profile") {
            val context = LocalContext.current
            ProfileScreen(
                userData = googleAuthUiClient.getSignedInUser(),
                onSignOut = {
                    lifecycleScope.launch {
                        googleAuthUiClient.signOut()
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_LONG).show()
                        navController.navigate("sign_in") {
                            popUpTo(0)
                        }
                    }
                }
            )
        }
    }
}