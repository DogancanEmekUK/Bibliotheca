package com.dogancanemek.bibliotheca.activities

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dogancanemek.bibliotheca.DarkColors
import com.dogancanemek.bibliotheca.LightColors
import com.dogancanemek.bibliotheca.properties.Story
import com.dogancanemek.bibliotheca.db
import kotlinx.coroutines.launch

class WritingArea {
    @Composable
    fun DisplayWritingArea() {
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

    private fun writeNewStory(title: String, story: String) {
        val newStory = Story(title, story)
        db.child("stories").child(title).setValue(newStory).addOnSuccessListener {
            Log.d("Success message", "Story added successfully")
        }.addOnFailureListener { exception ->
            Log.e("Failure message", "Failed to add story", exception)
        }
    }
}