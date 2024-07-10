package com.dogancanemek.bibliotheca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dogancanemek.bibliotheca.activities.Greetings
import com.dogancanemek.bibliotheca.activities.ReadByMe
import com.dogancanemek.bibliotheca.activities.ReadingArea
import com.dogancanemek.bibliotheca.activities.StoryList
import com.dogancanemek.bibliotheca.activities.WritingArea
import com.dogancanemek.bibliotheca.properties.ReadByUser
import com.dogancanemek.bibliotheca.properties.Story
import com.dogancanemek.bibliotheca.ui.theme.BibliothecaTheme
import com.dogancanemek.bibliotheca.ui.theme.MyDarkColor
import com.dogancanemek.bibliotheca.ui.theme.MyLightColor
import com.dogancanemek.bibliotheca.ui.theme.MyThemeColor
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

val db =
    Firebase.database("https://bibliotheca-621e8-default-rtdb.europe-west1.firebasedatabase.app/").reference
private const val TAG = "ReadAndWriteSnippets"

val LightColors =
    lightColorScheme(primary = MyLightColor, secondary = MyDarkColor, tertiary = MyThemeColor)
val DarkColors =
    darkColorScheme(primary = MyDarkColor, secondary = MyLightColor, tertiary = MyThemeColor)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BibliothecaTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppNavigation(
    paddingValues: PaddingValues,
    navController: NavHostController,
    username: String
) {
    var latestStory by remember { mutableStateOf<Story?>(null) }
    val auth = Firebase.auth
    val currentUser =
        remember { mutableStateOf(auth.currentUser) } // Remember initial state

    // Observe auth state changes, but don't change initial destination
    LaunchedEffect(Unit) {
        auth.addAuthStateListener { user ->
            currentUser.value = user.currentUser
        }
    }

    val user = currentUser.value

    NavHost(
        navController = navController,
        startDestination = if (user == null) "sign_in" else "story_list", // Set based on initial state
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("sign_in") { Greetings().SignInForm(navController) } // Sign-in route
        composable("sign_up") { Greetings().SignUpForm(navController) } // Sign-up route
        composable("story_list") {
            StoryList().DisplayStoryList(
                navController,
                username,
                latestStory
            ) { newLatestStory -> latestStory = newLatestStory }
        }
        composable("writing_area") { WritingArea().DisplayWritingArea() }
        composable("read_by_me") {
            ReadByMe().DisplayReadByMe(
                navController,
                username,
                latestStory
            ) { newLatestReadStory ->
                latestStory = newLatestReadStory
            }
        }
        composable("reading_area") {
            ReadingArea().DisplayReadingArea(latestStory)
        }
    }
}

@Composable
fun AppContent() {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) } // State for bottom bar visibility

    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val uid = currentUser?.uid

    val usersRef = db.child("users").child(uid ?: "")

    var username by remember { mutableStateOf("") }

    LaunchedEffect(uid) { // Trigger when UID changes
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                username = dataSnapshot.child("username").getValue(String::class.java) ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // Observe current route to control bottom bar visibility
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        showBottomBar = !(currentRoute == "sign_in" || currentRoute == "sign_up")
    }

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors
    ) {
        Scaffold(
            containerColor = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary,
            bottomBar = {
                if (showBottomBar) { // Conditionally show bottom bar
                    BottomNavigator().BottomNavigationBar(navController)
                }
            }
        ) { paddingValues ->
            AppNavigation(paddingValues, navController, username)
        }
    }
}