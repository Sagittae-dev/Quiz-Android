package com.example.quizandroid

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

abstract class BaseActivity : AppCompatActivity() {
    //zmiana uzytkownika
    //cykl zycia listenerow logowania
    //logowanie
    //wynik z logowania
    //sukces i porazka z logowania
    private val  baseAuthStateListener: FirebaseAuth.AuthStateListener by lazy {
        FirebaseAuth.AuthStateListener { firebaseAuth ->
            QuizClass.fUser = firebaseAuth.currentUser
        }
    }

    override fun onPause() {
        super.onPause()
        QuizClass.fAuth.addAuthStateListener(baseAuthStateListener)
    }

    override fun onResume() {
        super.onResume()
        QuizClass.fAuth.removeAuthStateListener(baseAuthStateListener)
    }
    fun logIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this,gso)
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN&& resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                QuizClass.fAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this,{
                        task ->
                        if (task.isSuccessful){
                            onLogInSuccess()
                        }
                        else{
                            onLogFail(task.exception)
                        }
                    })
            }catch (e: ApiException){
                Log.w("base activity", "google sign in failed")
            }

        }
    }

    open fun onLogInSuccess() {
        Log.d("base activity","login succes")
    }

    open fun onLogFail(exception: Exception?) {
        Log.d("base activity","login failed")

    }

    companion object{
    const val  RC_SIGN_IN = 1232
}
}