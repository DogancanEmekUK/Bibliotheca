package com.dogancanemek.bibliotheca

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ButtonColors
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.contentColorFor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dogancanemek.bibliotheca.ui.theme.BibliothecaTheme
import com.dogancanemek.bibliotheca.ui.theme.MyDarkColor
import com.dogancanemek.bibliotheca.ui.theme.MyLightColor
import com.dogancanemek.bibliotheca.ui.theme.MyThemeColor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

val db =
    Firebase.database("https://bibliotheca-621e8-default-rtdb.europe-west1.firebasedatabase.app/").reference
private const val TAG = "ReadAndWriteSnippets"

private val LightColors =
    lightColorScheme(primary = MyLightColor, secondary = MyDarkColor, tertiary = MyThemeColor)
private val DarkColors =
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
fun AppNavigation(paddingValues: PaddingValues, navController: NavHostController) {
    var latestStory by remember { mutableStateOf<Stories?>(null) }
    NavHost(
        navController = navController,
        startDestination = "auth_options",
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("auth_options") { AuthOptions() }
        composable("story_list") {
            StoryList(navController, latestStory) { newLatestStory ->
                latestStory = newLatestStory
            }
        }
        composable("writing_area") { WritingArea() }
        composable("read_by_me") { ReadByMe() }
        // Add a route for ReadingArea with arguments
        composable("reading_area") {
            ReadingArea(latestStory) // Pass only latestStory
        }
    }
}

@Composable
fun AppContent() {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) } // State for bottom bar visibility

    // Observe current route to control bottom bar visibility
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        showBottomBar = currentRoute != "auth_options"
    }

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors
    ) {
        Scaffold(
            containerColor = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary,
            bottomBar = {
                if (showBottomBar) { // Conditionally show bottom bar
                    BottomNavigationBar(navController)
                }
            }
        ) { paddingValues ->
            AppNavigation(paddingValues, navController)
        }
    }
}

@Composable
fun AuthOptions() {
    var showSignUp by remember { mutableStateOf(true) } // Toggle between sign-up and sign-in

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showSignUp) {
            SignUpForm()
            Button(onClick = { showSignUp = false }) {
                Text("Already have an account? Sign in")
            }
        } else {
            SignInForm()
            Button(onClick = { showSignUp = true }) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}

@Composable
fun SignUpForm() {
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            TextField(value = userName, onValueChange = { userName = it }, label = { Text("Username") })
            TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            Button(onClick = {
                // Add user to Firebase
                val usersRef = db.child("users").child(userName)
                usersRef.child("password").setValue(password)
                    .addOnSuccessListener {
                        scope.launch {
                            snackbarHostState.showSnackbar("Sign up successful!")
                        }
                    }
                    .addOnFailureListener { exception ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Sign up failed: ${exception.message}")
                        }
                    }
            }) {
                Text("Sign Up")
            }
        }
    }
}

@Composable
fun SignInForm() {
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            TextField(value = userName, onValueChange = { userName = it }, label = { Text("Username") })
            TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            Button(onClick = {

            }) {
                Text("Sign In")
            }
        }
    }
}

@Composable
fun StoryList(
    navController: NavHostController, latestStory: Stories?, onStoryClick: (Stories) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        // Display stories from Firebase (you'll need to implement this part)
        StoriesFromFirebase(navController, onStoryClick)
    }
}

fun writeNewStory(title: String, story: String) {
    val newStory = Stories(title, story)
    db.child("stories").child(title).setValue(newStory).addOnSuccessListener {
        Log.d(TAG, "Story added successfully")
    }.addOnFailureListener { exception ->
        Log.e(TAG, "Failed to add story", exception)
    }
}

@Composable
fun StoriesFromFirebase(navController: NavHostController, onStoryClick: (Stories) -> Unit) {
    var stories by remember { mutableStateOf<List<Stories>>(emptyList()) }

    // Fetch stories from Firebase when the composable enters the composition
    LaunchedEffect(Unit) {
        val storiesRef = db.child("stories")
        storiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fetchedStories = mutableListOf<Stories>()
                for (storySnapshot in dataSnapshot.children) {
                    val story = storySnapshot.getValue(Stories::class.java)
                    story?.let { fetchedStories.add(it) }
                }
                stories = fetchedStories
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadStories:onCancelled", databaseError.toException())
            }
        })
    }

    LazyColumn {
        items(stories) { story ->
            Column(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clipToBounds()
                .border(
                    1.dp,
                    if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable {
                    // Navigate to ReadingArea with title and story
                    onStoryClick(story)
                    navController.navigate("reading_area")
                }) {
                story.title?.let {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                story.story?.let {
                    Text(
                        text = it.split(" ").take(3).joinToString(" ") + "...",
                        fontSize = 14.sp,
                        color = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WritingArea() {
    var title by remember { mutableStateOf("") }
    var story by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(modifier = Modifier
        .fillMaxSize(),
        containerColor = (if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary),
        snackbarHost = { SnackbarHost(snackbarHostState) }) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(8.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        "Enter your title",
                        color = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary
                    ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                    unfocusedTextColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                    cursorColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                    focusedContainerColor = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary,
                    unfocusedContainerColor = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                value = story,
                onValueChange = { story = it },
                textStyle = MaterialTheme.typography.labelSmall,
                label = {
                    Text(
                        "Enter your story",
                        color = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        1.dp,
                        if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary
                    ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                    unfocusedTextColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                    cursorColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                    focusedContainerColor = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary,
                    unfocusedContainerColor = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary),
                onClick = {
                    if (title.isNotBlank() && story.isNotBlank()) {
                        writeNewStory(title, story)
                        scope.launch {
                            snackbarHostState.showSnackbar("Story added: $title")
                        }
                        // Clear the text fields after adding
                        title = ""
                        story = ""
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter both title and story")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Add Story",
                    color = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ReadByMe() {

}

@Composable
fun ReadingArea(latestStory: Stories?) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .border(1.dp, if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary)
    ) {
        if (latestStory != null) {
            Text(
                text = "${latestStory.title}", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "${latestStory.story}",
                fontSize = 14.sp,
                color = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.dp,
                        if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary
                    ), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Please choose a story",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(16.dp),
                    color = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomNavigation(
        backgroundColor = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary
    ) {
        BottomNavigationItem(icon = { /* Icon for notes */ }, label = {
            Text(
                "Story List",
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Serif,
                color = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary
            )
        }, selected = false,
            onClick = { navController.navigate("story_list") })
        BottomNavigationItem(icon = { /* Icon for screen 2 */ }, label = {
            Text(
                "Writing Area",
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Serif,
                color = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary
            )
        }, selected = false,
            onClick = { navController.navigate("writing_area") })
        BottomNavigationItem(icon = { /* Icon for screen 3 */ }, label = {
            Text(
                "Read By Me",
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Serif,
                color = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary
            )
        }, selected = false,
            onClick = { navController.navigate("read_by_me") })
        BottomNavigationItem(icon = { /* Icon for screen 4 */ }, label = {
            Text(
                "Reading Area",
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Serif,
                color = if (isSystemInDarkTheme()) DarkColors.primary else LightColors.primary
            )
        }, selected = false,
            onClick = { navController.navigate("reading_area") })
    }
}