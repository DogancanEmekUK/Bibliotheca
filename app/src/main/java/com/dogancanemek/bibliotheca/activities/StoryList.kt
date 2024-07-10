package com.dogancanemek.bibliotheca.activities

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dogancanemek.bibliotheca.DarkColors
import com.dogancanemek.bibliotheca.LightColors
import com.dogancanemek.bibliotheca.properties.ReadByUser
import com.dogancanemek.bibliotheca.properties.Story
import com.dogancanemek.bibliotheca.db
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class StoryList {
    @Composable
    fun DisplayStoryList(
        navController: NavHostController,
        username: String,
        latestStory: Story?,
        onStoryClick: (Story) -> Unit
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            // Display stories from Firebase (you'll need to implement this part)
            StoriesFromFirebase(navController, username, onStoryClick)
        }
    }

    @Composable
    fun StoriesFromFirebase(
        navController: NavHostController,
        username: String,
        onStoryClick: (Story) -> Unit
    ) {
        var stories by remember { mutableStateOf<List<Story>>(emptyList()) }

        // Fetch stories from Firebase when the composable enters the composition
        LaunchedEffect(Unit) {
            val storiesRef = db.child("stories")
            storiesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val fetchedStories = mutableListOf<Story>()
                    for (storySnapshot in dataSnapshot.children) {
                        val story = storySnapshot.getValue(Story::class.java)
                        story?.let { fetchedStories.add(it) }
                    }
                    stories = fetchedStories
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Cancellation message", "loadStories:onCancelled", databaseError.toException())
                }
            })
        }

        LazyColumn {
            items(stories) { story ->
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clipToBounds()
                        .border(
                            1.dp,
                            if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable {
                            onStoryClick(story)
                            navController.navigate("reading_area")
                        },
                    horizontalArrangement = Arrangement.SpaceBetween // Position items on opposite ends
                ) {
                    Column(modifier = Modifier.weight(1f)) { // Text takes available space
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
                    // Add your clickable icon here
                    IconButton(onClick = {
                        story.title?.let {
                            story.story?.let { it1 ->
                                addToReadByMe(
                                    username,
                                    it,
                                    it1
                                )
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Add to Read By Me",
                            tint = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary
                        )
                    }
                }
            }
        }
    }

    private fun addToReadByMe(username: String, title: String, story: String) {
        if (username.isNotBlank()) {
            val readStory = Story(title, story, readBy = username)
            db.child("read_by_me").child(username).push().setValue(readStory)
                .addOnSuccessListener {
                    Log.d("Success message", "Story added successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("Failure message", "Failed to add story", exception)
                }
        }
    }
}