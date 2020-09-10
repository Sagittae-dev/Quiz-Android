package com.example.quizandroid

import com.example.quizandroid.profile.UserItem
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toUserItem(): UserItem{
    return UserItem().apply {
        uid =this@toUserItem.uid
        url = this@toUserItem.photoUrl.toString()
        name = this@toUserItem.displayName!!
    }
}