package com.kw.yuseyun_2020

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainRvAdapter(var context: Context, val items : ArrayList<String>):
        RecyclerView.Adapter<MainRvAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_recyclerview, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(items[position], context)
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val guidance_sentence = itemView?.findViewById<TextView>(R.id.textView_guidanceSentence)

        fun bind (str: String, context: Context) {
            guidance_sentence?.text = str;
        }
    }
    fun addSentences(sentence:ArrayList<String>) {
        this.items.addAll(sentence);
    }
}