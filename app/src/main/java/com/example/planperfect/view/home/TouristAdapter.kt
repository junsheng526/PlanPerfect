package com.example.planperfect.view.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planperfect.data.model.Tourist
import com.example.planperfect.databinding.ItemTouristBinding

class TouristAdapter(private val tourists: List<Tourist>) :
    RecyclerView.Adapter<TouristAdapter.TouristViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TouristViewHolder {
        val binding = ItemTouristBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TouristViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TouristViewHolder, position: Int) {
        holder.bind(tourists[position])
    }

    override fun getItemCount(): Int = tourists.size

    class TouristViewHolder(private val binding: ItemTouristBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tourist: Tourist) {
            binding.touristTitle.text = tourist.title
            binding.touristImage.setImageResource(tourist.imageResId)
        }
    }
}
