package com.example.quizandroid

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.quizandroid.chooser.QuizChooserFragment
import com.example.quizandroid.chooser.Quizitem
import com.example.quizandroid.news.NewsItem
import com.example.quizandroid.news.NewsListFragment
import com.example.quizandroid.profile.OtherProfileActivity
import com.example.quizandroid.profile.ProfileFragment
import com.example.quizandroid.profile.UserItem
import com.example.quizandroid.questionset.QuestionItem
import com.example.quizandroid.questionset.QuizActivity
import com.example.quizandroid.summary.QuizSummaryActivity
import com.example.quizandroid.summary.QuizSummaryActivity.Companion.NEW_FEED
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_newsitem_list.*
import kotlinx.android.synthetic.main.fragment_quizitem_list.*

class MainActivity : BaseActivity(),
    QuizChooserFragment.OnStartQuizListener,
    NewsListFragment.OnNewsInteractionListener,
    ProfileFragment.OnLogChangeListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setViewPager()
    }
// region ustawiania pagera i nawigation
    private fun setViewPager() {
        viewpager.adapter = getFragmentPagerAdapter()
        navigation.setOnNavigationItemSelectedListener(getBottomNavigationItemSelectedListener())
        viewpager.addOnPageChangeListener(getOnPageChangeListener())
        viewpager.offscreenPageLimit =2
    }

    private fun getFragmentPagerAdapter()=
        object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment = when (position) {
                FEED_ID -> NewsListFragment()
                CHOOSER_ID -> QuizChooserFragment()
                PROFILE_ID-> ProfileFragment()
                else ->{
                    Log.wtf("Fragment out of bounds","how came?")
                    Fragment()
                }
            }
            override fun getCount():Int = 3
        }
    private fun getBottomNavigationItemSelectedListener()=
        BottomNavigationView.OnNavigationItemSelectedListener { item->
            when (item.itemId) {
                R.id.navigation_home -> {
                    viewpager.currentItem = 0
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_dashboard ->{
                    viewpager.currentItem = 1
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifications ->{
                    viewpager.currentItem = 2
                    return@OnNavigationItemSelectedListener true
                }else -> return@OnNavigationItemSelectedListener false
            }
        }

    private fun getOnPageChangeListener() =
        object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                navigation.menu.getItem(position).isChecked = true
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when {
                (requestCode == QUIZ_ACT_REQ_CODE) -> {
                    navigateToSummaryActivity(data)
                }
                (requestCode == QUIZ_SUMMARY_RCODE)->{
                    pushNewNews(data)
                }
            }
        }
    }

    private fun pushNewNews(data: Intent?) {
        val feedItem = data!!.extras?.get(NEW_FEED) as NewsItem
        QuizClass.fData.getReference("feeds").push().setValue(feedItem.apply {
            uid = QuizClass.fUser!!.uid
            image = QuizClass.fUser!!.photoUrl.toString()
            user = QuizClass.fUser!!.displayName!!
        })
        viewpager.currentItem = 0
        getNewsListFragment().feed_item_list.smoothScrollToPosition(0)
    }

    private fun navigateToSummaryActivity(data: Intent?) {
        var intent = Intent(this, QuizSummaryActivity::class.java).apply {
            if(QuizClass.fUser!= null){
                data?.putExtra(USER_NAME, QuizClass.fUser?.displayName?: QuizClass.res.getString(R.string.anonym_name))
                data?.putExtra(USER_URL,QuizClass.fUser?.photoUrl.toString() )
            }
            putExtras(data!!.extras!!)
        }
        startActivityForResult(intent,QUIZ_SUMMARY_RCODE)
    }
    //endregion

    private fun getChooserListFragment()=
        (supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.viewpager + ":"+ CHOOSER_ID) as QuizChooserFragment)
    private fun getNewsListFragment()=
        (supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.viewpager + ":"+ FEED_ID) as NewsListFragment)

    override fun OnStartQuizSelected(quiz: Quizitem, name: String) {
        getChooserListFragment().loader_quiz.visibility = View.VISIBLE
        QuizClass.fData.getReference("questions/${quiz.questset}").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                val quizset = ArrayList<QuestionItem>()
                p0.children.map { it.getValue(QuestionItem::class.java) }.mapTo(quizset){it!!}
                getChooserListFragment().loader_quiz.visibility = View.GONE
                navigateQuiz(quizset,name,quiz)
            }
        })

    }

    fun navigateQuiz(quizSet: ArrayList<QuestionItem>,title: String, quiz: Quizitem){
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra(QUIZ_SET,quizSet)
            putExtra(TITLE,title)
            putExtra(QUIZ,quiz)
        }
        startActivityForResult(intent, QUIZ_ACT_REQ_CODE)
    }
    override fun onUserSelected(user: UserItem, image: View) {
        val intent =  Intent(this,OtherProfileActivity::class.java)
        intent.putExtra(USER_ITEM, user)
        val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,image,"circleProfileImageTransition")
        startActivity(intent,optionsCompat.toBundle())
    }

    override fun onLikeSelected(feedId: String, diff: Int) {
        if (QuizClass.fUser != null)
            QuizClass.fData.getReference("feeds/$feedId/respects").updateChildren(mapOf(Pair(QuizClass.fUser?.uid,diff)))
                .addOnCompleteListener { Log.d("MainActivity","Just liked$feedId, with$diff") }
    }

    override fun onLogOut() {
        QuizClass.fAuth.signOut()
        //getNewsListFragment().feed_item_list?.adapter?.notifyDataSetChanged()
    }

    override fun onLogIn() {
        logIn()
    }

    //endregion
    companion object{
        const val FEED_ID = 0
        const val CHOOSER_ID= 1
        const val PROFILE_ID= 2

        const val QUIZ_SET = "quiz_set"
        const val TITLE = "TITLE"
        const val QUIZ = "QUIZ"
        const val QUIZ_ACT_REQ_CODE = 324
        const val USER_ITEM = "USER_ITEM"
        const val USER_NAME = "USER_NAME"
        const val USER_URL = "USER_URL"
        const val QUIZ_SUMMARY_RCODE = 2431
    }
}
