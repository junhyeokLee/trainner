package com.sports2i.trainer.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder.decodeBitmap
import android.util.Base64
import androidx.core.content.edit
import com.sports2i.trainer.utils.FileUtil.decodeBitmapFromString

object Preferences {
    const val DB_USER_INFO = "user_info"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_GROUP_ID = "groupId"
    const val KEY_EMAIL = "email"
    const val KEY_AUTHORITY = "authority"
    const val KEY_BIO_ACTIVATED = "bio_activated"
    const val KEY_ORGANIZATION_ID = "organization_id"
    const val KEY_PROFILE_URL = "profile_url"
    const val KEY_AUTO_LOGIN = "auto_login"

    const val KEY_GROUP_MORE_VIEW_CLICCKED = "group_more_view_clicked"

    private lateinit var preferences: SharedPreferences

    fun init(context: Context, name: String) {
        preferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE)
    }

    fun string(key: String): String {
        return preferences.getString(key, "")!!
    }

    fun int(key: String): Int {
        return preferences.getInt(key, 0)
    }

    fun boolean(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    fun put(key: String, value: String) {
        preferences.edit {
            putString(key, value)
        }
    }

    fun put(key: String, value: Int) {
        preferences.edit {
            putInt(key, value)
        }
    }

    fun put(key: String, value: Boolean) {
        preferences.edit {
            putBoolean(key, value)
        }
    }

    fun clear() {
        preferences.edit {
            clear()
        }
    }

}