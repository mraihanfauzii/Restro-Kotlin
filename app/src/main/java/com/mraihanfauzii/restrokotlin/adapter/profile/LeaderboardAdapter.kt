package com.mraihanfauzii.restrokotlin.adapter.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.ItemLeaderboardBinding
import com.mraihanfauzii.restrokotlin.model.LeaderboardEntry

class LeaderboardAdapter : ListAdapter<LeaderboardEntry, LeaderboardAdapter.LeaderboardViewHolder>(
    LeaderboardDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, position)
    }

    inner class LeaderboardViewHolder(private val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LeaderboardEntry, position: Int) {
            binding.tvRank.text = (position + 1).toString()
            binding.tvUsername.text = entry.username
            binding.tvFullName.text = entry.fullName
            binding.tvTotalPoints.text = "${entry.totalPoints} Poin"

            entry.highestBadgeInfo?.let { badge ->
                binding.tvBadgeName.text = badge.name
                Glide.with(binding.ivBadge.context)
                    .load(badge.imageUrl)
                    .error(R.drawable.baseline_error_24)
                    .into(binding.ivBadge)
            } ?: run {
                binding.tvBadgeName.text = "Tidak Ada Badge"
            }
        }
    }

    class LeaderboardDiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem == newItem
        }
    }
}