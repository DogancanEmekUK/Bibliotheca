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
import com.dogancanemek.bibliotheca.db
import com.dogancanemek.bibliotheca.properties.Story
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ReadByMe {
    @Composable
    fun DisplayReadByMe(
        navController: NavHostController,
        username: String,
        latestStory: Story?,
        onStoryClick: (Story) -> Unit
    ) {
        if (username.isNotBlank()) { // Check for non-empty username
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                ReadStoriesFromFirebase(navController, username, onStoryClick)
            }
        } else {
            // Handle case where username is empty
            Text("Please login or sign up")
        }
    }

    @Composable
    fun ReadStoriesFromFirebase(
        navController: NavHostController,
        username: String,
        onStoryClick: (Story) -> Unit,
    ) {
        var storiesReadByMe by remember { mutableStateOf<List<Story>>(emptyList()) }
        LaunchedEffect(Unit){
            val readByMeRef = db.child("read_by_me").child(username)
            readByMeRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val fetchedStories = mutableListOf<Story>()
                    for (storySnapshot in dataSnapshot.children) {
                        val story = storySnapshot.getValue(Story::class.java)
                        // Filter stories based on readBy field
                        if (story?.readBy == username) {
                            story.let { fetchedStories.add(it) }
                        }
                    }
                    storiesReadByMe = fetchedStories
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Cancellation message", "loadStories:onCancelled", databaseError.toException())
                }
            })
        }

        LazyColumn {
            items(storiesReadByMe) { storyReadByMe ->
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
                            onStoryClick(storyReadByMe)
                            navController.navigate("reading_area")
                        },
                    horizontalArrangement = Arrangement.SpaceBetween // Position items on opposite ends
                ) {
                    Column(modifier = Modifier.weight(1f)) { // Text takes available space
                        storyReadByMe.title?.let {
                            Text(
                                text = it,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        storyReadByMe.story?.let {
                            Text(
                                text = storyReadByMe.story.split(" ").take(3).joinToString(" ") + "...",
                                fontSize = 14.sp,
                                color = if (isSystemInDarkTheme()) LightColors.primary else DarkColors.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}