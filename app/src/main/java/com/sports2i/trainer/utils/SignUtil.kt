package com.sports2i.trainer.utils

import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.provider.Settings.System.DATE_FORMAT
import com.sports2i.trainer.data.model.GroupInfo
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object SignUtil {
    // 특수문자 존재 여부를 확인하는 메서드
    private fun hasSpecialCharacter(string: String): Boolean {
        for (i in string.indices) {
            if (!Character.isLetterOrDigit(string[i])) {
                return true
            }
        }
        return false
    }


    // 영문자 존재 여부를 확인하는 메서드
    private fun hasAlphabet(string: String): Boolean {
        for (i in string.indices) {
            if (Character.isAlphabetic(string[i].code)) {
                return true
            }
        }
        return false
    }

    // 위의 두 메서드를 포함하여 종합적으로 id 정규식을 확인하는 메서드
    fun idRegex(id: String): Boolean {
        if ((!hasSpecialCharacter(id)) and (hasAlphabet(id)) and (id.length >= 5)) {
            return true
        }
        return false
    }


    // 숫자 존재 여부를 확인하는 메서드
    private fun hasDigit(string: String): Boolean {
        for (i in string.indices) {
            if (Character.isDigit(string[i])) {
                return true
            }
        }
        return false
    }

    // 위의 세 메서드를 포함하여 종합적으로 비밀번호 정규식을 확인하는 메서드
    // "비밀번호는 8자 이상의 영문,숫자,특수문자로 구성되어야 합니다."
    fun isPasswordValid(password: String): Boolean {
        return (password.length >= 8) && hasAlphabet(password) && hasDigit(password) && hasSpecialCharacter(password)
    }
}