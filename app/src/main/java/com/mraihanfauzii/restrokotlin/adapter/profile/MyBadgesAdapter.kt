package com.mraihanfauzii.restrokotlin.adapter.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.ItemMyBadgeBinding
import com.mraihanfauzii.restrokotlin.model.UserBadge

class MyBadgesAdapter : ListAdapter<UserBadge, MyBadgesAdapter.MyBadgeViewHolder>(
    MyBadgeDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBadgeViewHolder {
        val binding = ItemMyBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyBadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyBadgeViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class MyBadgeViewHolder(private val binding: ItemMyBadgeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userBadge: UserBadge) {
            binding.tvBadgeName.text = userBadge.badgeInfo.name
            binding.tvBadgeDescription.text = userBadge.badgeInfo.description
            binding.tvAwardedAt.text = "Diperoleh: ${userBadge.awardedAt.split("T")[0]}" // Simple date format

            Glide.with(binding.ivBadge.context)
                .load(userBadge.badgeInfo.imageUrl)
                .error(R.drawable.baseline_error_24)
                .into(binding.ivBadge)
        }
    }

    class MyBadgeDiffCallback : DiffUtil.ItemCallback<UserBadge>() {
        override fun areItemsTheSame(oldItem: UserBadge, newItem: UserBadge): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserBadge, newItem: UserBadge): Boolean {
            return oldItem == newItem
        }
    }
}