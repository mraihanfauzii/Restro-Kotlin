package com.mraihanfauzii.restrokotlin.adapter.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mraihanfauzii.restrokotlin.databinding.ItemMovementDetailBinding
import com.mraihanfauzii.restrokotlin.model.DetailGerakan

class MovementDetailAdapter : ListAdapter<DetailGerakan, MovementDetailAdapter.MovementDetailViewHolder>(
    MovementDetailDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovementDetailViewHolder {
        val binding = ItemMovementDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovementDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovementDetailViewHolder, position: Int) {
        val movement = getItem(position)
        holder.bind(position + 1, movement) // Pass position + 1 for numbering
    }

    class MovementDetailViewHolder(private val binding: ItemMovementDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(index: Int, detail: DetailGerakan) {
            with(binding) {
                tvMovementName.text = "$index. ${detail.nama ?: "-"}"
                tvMovementOk.text = "Sempurna: ${detail.ok ?: 0}"
                tvMovementNg.text = "Tidak Sempurna: ${detail.ng ?: 0}"
                tvMovementUndetected.text = "Tidak Terdeteksi: ${detail.undetected ?: 0}"
                tvMovementDuration.text = "Durasi: ${detail.durasi ?: 0}s"

                // Optional: set icon colors if you want them to match the text color
                // val context = itemView.context
                // tvMovementOk.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_check_circle), null, null, null)
                // tvMovementNg.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_cancel), null, null, null)
                // tvMovementUndetected.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_warning), null, null, null)
                // tvMovementDuration.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_time), null, null, null)
            }
        }
    }

    private class MovementDetailDiffCallback : DiffUtil.ItemCallback<DetailGerakan>() {
        override fun areItemsTheSame(oldItem: DetailGerakan, newItem: DetailGerakan): Boolean {
            return oldItem.laporanGerakanId == newItem.laporanGerakanId // Assuming laporanGerakanId is unique
        }

        override fun areContentsTheSame(oldItem: DetailGerakan, newItem: DetailGerakan): Boolean {
            return oldItem == newItem // Data class equality
        }
    }
}