package com.mraihanfauzii.restrokotlin.adapter.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.ItemAllBadgeBinding
import com.mraihanfauzii.restrokotlin.model.BadgeInfo

class AllBadgesAdapter : ListAdapter<BadgeInfo, AllBadgesAdapter.AllBadgeViewHolder>(
    AllBadgeDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllBadgeViewHolder {
        val binding = ItemAllBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllBadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllBadgeViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class AllBadgeViewHolder(private val binding: ItemAllBadgeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(badge: BadgeInfo) {
            binding.tvBadgeName.text = badge.name
            binding.tvBadgeDescription.text = badge.description
            binding.tvPointThreshold.text = "Min Poin: ${badge.pointThreshold}"

            Glide.with(binding.ivBadge.context)
                .load(badge.imageUrl)
                .error(R.drawable.baseline_error_24)
                .into(binding.ivBadge)
        }
    }

    class AllBadgeDiffCallback : DiffUtil.ItemCallback<BadgeInfo>() {
        override fun areItemsTheSame(oldItem: BadgeInfo, newItem: BadgeInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BadgeInfo, newItem: BadgeInfo): Boolean {
            return oldItem == newItem
        }
    }
}