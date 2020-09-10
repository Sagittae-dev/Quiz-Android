package com.example.quizandroid.chooser

import java.io.Serializable

data class Quizitem(
    var level:LevelEnum = LevelEnum.EASY,
    var lang:LangEnum = LangEnum.ANDROID,
    var questset: String ="") : Serializable