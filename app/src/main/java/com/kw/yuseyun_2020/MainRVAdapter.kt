package com.kw.yuseyun_2020

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainRvAdapter(var context: Context, val items : ArrayList<Guidance>):
        RecyclerView.Adapter<MainRvAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_recyclerview, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        var g = items[position]
        holder?.bind(g, context)
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val guidance_sentence = itemView?.findViewById<TextView>(R.id.textView_guidanceSentence)
        val guidance_index = itemView?.findViewById<TextView>(R.id.textView_guidanceIndex)
        val guidance_image = itemView?.findViewById<ImageView>(R.id.imageView_guidance)

        fun bind (g: Guidance, context: Context) {
            if (g.index.contentEquals("출발") ) {
                guidance_image?.setBackgroundResource(R.drawable.start_for_guidance)
                guidance_index?.setBackgroundResource(R.drawable.empty_white_rectangle)
                guidance_index?.text = "🏃‍"

            }else if (g.index.contentEquals("도착")) {
                guidance_image?.setBackgroundResource(R.drawable.end_for_guidance)
                guidance_index?.setBackgroundResource(R.drawable.empty_white_rectangle)
                guidance_index?.text = "😉"
            } else {
                guidance_index?.setBackgroundResource(R.drawable.guidance_index)
                if (g.direction == 0) guidance_image?.setBackgroundResource(R.drawable.go_straight_arrow)
                else if (g.direction == 1) guidance_image?.setBackgroundResource(R.drawable.turn_left_arrow)
                else if (g.direction == 2) guidance_image?.setBackgroundResource(R.drawable.turn_right_arrow)

                guidance_index?.text = g.index
            }
            guidance_sentence?.text = g.sentence
            /*guidance_index?.text = g.index
            guidance_sentence?.text = g.sentence*/
        }
    }
    fun addGuidances(guidances:ArrayList<Guidance>) {
        this.items.clear();
        this.items.addAll(guidances);
    }
}