package com.example.quizandroid.chooser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quizandroid.R

class QuizChooserRecyclerViewAdapter(private val quizesMap: HashMap<String,Quizitem>,
                                    private val OnStartQuizListener:QuizChooserFragment.OnStartQuizListener):RecyclerView.Adapter<QuizChooserRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_quizitem,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = quizesMap.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val sorted  = quizesMap.values.toList().sortedBy { quizItem -> (quizItem.level.ordinal + quizItem.lang.ordinal*10)  }
        holder.mItem = sorted[position]
        holder.levelImageView.setImageResource(sorted[position].level.image)
        holder.langImageView.setImageResource(sorted[position].lang.image)
        holder.quizTitle.text = getDoubleLineQuizTitle(sorted,position)
        holder.mView.setOnClickListener {
            OnStartQuizListener.OnStartQuizSelected(holder.mItem, getSingleLineQuizTitle(sorted,position))
        }
    }
    private fun getSingleLineQuizTitle(sorted: List<Quizitem>, position: Int) =
        "${sorted[position].lang.getString()} \n ${sorted[position].level.getString()}"

    private fun getDoubleLineQuizTitle(sorted: List<Quizitem>, position: Int) =
        "${sorted[position].lang.getString()} \n ${sorted[position].level.getString()}"

    inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
        val levelImageView = mView.findViewById<View>(R.id.levelImageView) as ImageView
        val langImageView = mView.findViewById<View>(R.id.langImageView) as ImageView
        val quizTitle = mView.findViewById<View>(R.id.quizTitle) as TextView
        lateinit var mItem:Quizitem
    }
}