package com.dogancanemek.bibliotheca.activities

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dogancanemek.bibliotheca.DarkColors
import com.dogancanemek.bibliotheca.LightColors
import com.dogancanemek.bibliotheca.properties.Story

class ReadingArea {
    @Composable
    fun DisplayReadingArea(latestStory: Story?) {
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
}