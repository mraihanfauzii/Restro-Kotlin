package com.mraihanfauzii.restrokotlin.adapter.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mraihanfauzii.restrokotlin.databinding.ItemMovementPreparationBinding
import com.mraihanfauzii.restrokotlin.model.MovementDetail

class MovementListAdapter :
    ListAdapter<MovementDetail, MovementListAdapter.MovementViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovementViewHolder {
        val binding = ItemMovementPreparationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovementViewHolder, position: Int) {
        val movement = getItem(position)
        holder.bind(movement)
    }

    inner class MovementViewHolder(private val binding: ItemMovementPreparationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movement: MovementDetail) {
            binding.tvMovementName.text = movement.movementName ?: "Nama Gerakan Tidak Diketahui"
            binding.tvMovementDescription.text = movement.description ?: "Tidak ada deskripsi."
            binding.tvMovementReps.text = "Repetisi: ${movement.jumlahRepetisiDirencanakan ?: 0} kali"
            binding.tvMovementOrder.text = (movement.urutanDalamProgram ?: 0).toString()

            // Memuat GIF menggunakan Glide
            movement.imageUrl?.let { url ->
                if (url.endsWith(".gif", ignoreCase = true)) {
                    Glide.with(binding.ivMovementGif.context)
                        .asGif() // Penting untuk GIF
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder saat loading
                        .error(android.R.drawable.ic_delete) // Gambar error jika gagal
                        .into(binding.ivMovementGif)
                } else {
                    // Jika bukan GIF, muat sebagai gambar biasa
                    Glide.with(binding.ivMovementGif.context)
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(binding.ivMovementGif)
                }
            } ?: run {
                binding.ivMovementGif.setImageResource(android.R.drawable.ic_dialog_alert) // Gambar default jika URL null
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MovementDetail>() {
            override fun areItemsTheSame(oldItem: MovementDetail, newItem: MovementDetail): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MovementDetail, newItem: MovementDetail): Boolean {
                return oldItem == newItem
            }
        }
    }
}