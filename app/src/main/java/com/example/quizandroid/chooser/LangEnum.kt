package com.example.quizandroid.chooser

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizandroid.QuizClass
import com.example.quizandroid.R

enum class LangEnum(@StringRes val label: Int,
                    @DrawableRes val image: Int) {
    ANDROID(R.string.lang_android, R.drawable.ic_language_android),
    KOTLIN(R.string.lang_kotlin, R.drawable.ic_language_kotlin),
    JAVA(R.string.lang_java, R.drawable.ic_language_java);

    fun getString() = QuizClass.res.getString(this.label)
}