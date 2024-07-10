package com.dogancanemek.bibliotheca.activities

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dogancanemek.bibliotheca.DarkColors
import com.dogancanemek.bibliotheca.LightColors
import com.dogancanemek.bibliotheca.properties.Users
import com.dogancanemek.bibliotheca.db
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class Greetings {
    @Composable
    fun SignUpForm(navController: NavHostController) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var userName by remember { mutableStateOf("") } // Add for extra user data
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val auth = Firebase.auth

        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { contentPadding ->
            Column(modifier = Modifier.padding(contentPadding)) {
                TextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Username") })
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") }
                )
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary),
                    onClick = {
                        // Create user with Firebase Authentication
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sign up successful, store additional user data
                                    val user = auth.currentUser
                                    val usersRef = db.child("users").child(user?.uid ?: "")
                                    usersRef.setValue(
                                        Users(
                                            userName,
                                            email
                                        )
                                    ) // Store username and email
                                        .addOnSuccessListener {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Sign up successful!")
                                                navController.navigate("story_list")
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to store user data: ${exception.message}")
                                            }
                                        }
                                } else {
                                    // Sign up failed
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Sign up failed: ${task.exception?.message}")
                                    }
                                }
                            }
                    }) {
                    Text("Sign Up")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("sign_in") }) {
                    Text("Already have an account? Sign in")
                }
            }
        }
    }

    @Composable
    fun SignInForm(navController: NavHostController) { // Add NavHostController parameter
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val auth = Firebase.auth

        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { contentPadding ->
            Column(modifier = Modifier.padding(contentPadding)) {
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary),
                    onClick = {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Sign in successful!")
                                        navController.navigate("story_list")

                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Sign in failed: ${task.exception?.message}")
                                    }
                                }
                            }
                    }) {
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("sign_up") }) {
                    Text("Don't have an account? Sign up")
                }
            }
        }
    }
}