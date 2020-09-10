package com.example.quizandroid.news

import java.io.Serializable

data class NewsItem(
    var comments: String = "",
    var points: Int = 0,
    var quiz : String = "",
    var image: String = "",
    var user: String = "",
    var timeMillis: Long = 0,
    var uid: String = "",
    var respects: HashMap<String,Int> = hashMapOf()):Serializable