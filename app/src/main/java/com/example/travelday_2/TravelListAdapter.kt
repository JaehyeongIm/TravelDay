package com.example.travelday

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.travelday_2.TravelListItem
import com.example.travelday_2.databinding.TravelListRowBinding


class TravelListAdapter(val items:ArrayList<TravelListItem>) :RecyclerView.Adapter<TravelListAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun OnItemClick(data:TravelListItem)
    }
    var itemClickListener:OnItemClickListener?=null
    inner class ViewHolder(val binding: TravelListRowBinding) : RecyclerView.ViewHolder(binding.root){
        init{
            binding.textView.setOnClickListener{
                itemClickListener?.OnItemClick(items[adapterPosition])

            }

        }
    }
    fun moveItem(oldPos:Int, newPos:Int){
        val item = items[oldPos]
        items.removeAt(oldPos)
        items.add(newPos,item)
        notifyItemMoved(oldPos, newPos)
    }
    fun removeItem(pos:Int){
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TravelListRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textView.text = item.country + "\n" + item.startDate + " ~ " + item.endDate

    }
}