package com.codecx.statussaver.data.repos.appRepo

import android.content.Context
import androidx.core.content.edit

class UserPreferences(context: Context) {

    private val preferences =  context.getSharedPreferences( "user_preferences", Context.MODE_PRIVATE)


    val userSelectedDirPath get() =  preferences.getString("user_selected_dir_path", "") ?:""


    fun updateSelectedDirPath(path: String) {
        preferences.edit {
            putString("user_selected_dir_path", path)
        }
    }


}