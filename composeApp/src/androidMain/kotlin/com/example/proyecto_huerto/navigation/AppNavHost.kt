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
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    googleAuthUiClient: GoogleAuthUiClient,
    lifecycleScope: LifecycleCoroutineScope
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "sign_in") {
        composable("sign_in") {
            val viewModel = viewModel<SignInViewModel>()
            val state by viewModel.state.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(key1 = Unit) {
                if(googleAuthUiClient.getSignedInUser() != null) {
                    navController.navigate("profile")
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
                            viewModel.onSignInResult(signInResult)
                        }
                    }
                }
            )

            LaunchedEffect(key1 = state.isSignInSuccessful) {
                if(state.isSignInSuccessful) {
                    Toast.makeText(
                        context,
                        "Sign in successful",
                        Toast.LENGTH_LONG
                    ).show()

                    navController.navigate("profile")
                    viewModel.resetState()
                }
            }

            LaunchedEffect(key1 = state.signInError) {
                state.signInError?.let {
                    error ->
                    Toast.makeText(
                        context,
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.clearError()
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
                onLoginClick = {
                    email, password ->
                    viewModel.signInWithEmailAndPassword(email, password)
                },
                onNavigateToSignUp = { navController.navigate("sign_up") }
            )
        }
        composable("sign_up") {
            val viewModel = viewModel<SignInViewModel>()
            val state by viewModel.state.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(key1 = state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    Toast.makeText(
                        context,
                        "Registro completado. Por favor, inicie sesión.",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.popBackStack()
                    viewModel.resetState()
                }
            }

            LaunchedEffect(key1 = state.signInError) {
                state.signInError?.let {
                    error ->
                    Toast.makeText(
                        context,
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.clearError()
                }
            }

            SignUpScreen(
                onSignUpClick = {
                    email, password ->
                    viewModel.signUpWithEmailAndPassword(email, password)
                },
                onNavigateToSignIn = { navController.popBackStack() }
            )
        }

        composable("profile") {
            val context = LocalContext.current
            ProfileScreen(
                userData = googleAuthUiClient.getSignedInUser(),
                onSignOut = {
                    lifecycleScope.launch {
                        googleAuthUiClient.signOut()
                        Toast.makeText(
                            context,
                            "Signed out",
                            Toast.LENGTH_LONG
                        ).show()

                        navController.popBackStack()
                    }
                }
            )
        }
    }
}