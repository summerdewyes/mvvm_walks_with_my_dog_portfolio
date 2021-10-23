package com.summerdewyes.mvvm_walks_with_my_dog.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.summerdewyes.mvvm_walks_with_my_dog.databinding.ItemRunBinding
import com.summerdewyes.mvvm_walks_with_my_dog.db.Run
import com.summerdewyes.mvvm_walks_with_my_dog.other.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : ListAdapter<Run, RunAdapter.RunViewHolder>(diffCallback) {

    inner class RunViewHolder(private val binding: ItemRunBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(run: Run) {
            Glide.with(binding.root).load(run.img).into(binding.ivRunImage)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }

            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            binding.tvAvgSpeed.text = avgSpeed

            val distanceInkm = "${run.distanceInMeters / 1000f}km"
            binding.tvDistance.text = distanceInkm

            binding.tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned}kcal"
            binding.tvCalories.text = caloriesBurned

            val journal = run.journal
            binding.tvJournal.text = journal

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RunViewHolder(
        ItemRunBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    /**
     * 기존의 데이터 리스트와 교체할 데이터 리스트를 비교
     */
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Run>() {
            //두 아이템이 동일한 아이템인가?
            override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
                return oldItem.id == newItem.id
            }

            //두 아이템이 동일한 내용물 인가?
            override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

        }
    }
}