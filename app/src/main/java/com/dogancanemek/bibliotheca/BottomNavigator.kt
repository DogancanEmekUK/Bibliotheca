package com.dogancanemek.bibliotheca

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

class BottomNavigator {
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
}