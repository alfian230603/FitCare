package com.app.fitcare.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.fitcare.databinding.ItemLogBinding
import com.app.fitcare.models.DailyLog

class HomeAdapter : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    private val logs = mutableListOf<DailyLog>()

    fun setData(data: List<DailyLog>) {
        logs.clear()
        logs.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount() = logs.size

    class HomeViewHolder(private val binding: ItemLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: DailyLog) {
            binding.tvDate.text = log.date
            binding.tvWaterIntake.text = "${log.waterIntake} ml"
            binding.tvStepCount.text = "${log.stepCount} langkah"
        }
    }
}
