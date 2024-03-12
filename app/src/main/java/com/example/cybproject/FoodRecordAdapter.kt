package com.example.cybproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cybproject.data.FoodRecord

class FoodRecordAdapter(private var foodRecords: List<FoodRecord>) :
    RecyclerView.Adapter<FoodRecordAdapter.FoodRecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodRecordViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_food_record, parent, false)
        return FoodRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodRecordViewHolder, position: Int) {
        val foodRecord = foodRecords[position]
        holder.bind(foodRecord)
    }

    override fun getItemCount(): Int {
        return foodRecords.size
    }

    fun updateData(newFoodRecords: List<FoodRecord>) {
        foodRecords = newFoodRecords
        notifyDataSetChanged()
    }

    class FoodRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val foodNameTextView: TextView = itemView.findViewById(R.id.foodNameTextView)

        fun bind(foodRecord: FoodRecord) {
            dateTextView.text = foodRecord.date
            foodNameTextView.text = foodRecord.foodName
        }
    }
}
