package com.mraihanfauzii.restrokotlin.adapter.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mraihanfauzii.restrokotlin.databinding.ItemCalendarEventBinding // Akan dibuat nanti
import com.mraihanfauzii.restrokotlin.model.CalendarProgramResponse
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarEventAdapter(private val onItemClick: (CalendarProgramResponse) -> Unit) :
    ListAdapter<CalendarProgramResponse, CalendarEventAdapter.CalendarEventViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarEventViewHolder {
        val binding = ItemCalendarEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarEventViewHolder, position: Int) {
        val program = getItem(position)
        holder.bind(program)
    }

    inner class CalendarEventViewHolder(private val binding: ItemCalendarEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(program: CalendarProgramResponse) {
            binding.tvProgramName.text = program.programName ?: "-"
            binding.tvProgramStatus.text = program.status ?: "-"
            binding.tvTherapistName.text = program.therapist?.fullName ?: "-"

            // Format tanggal jika ada
            program.programDate?.let { dateString ->
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    val date = inputFormat.parse(dateString)
                    binding.tvProgramDate.text = outputFormat.format(date)
                } catch (e: Exception) {
                    binding.tvProgramDate.text = dateString // Fallback jika format error
                }
            } ?: run {
                binding.tvProgramDate.text = "-"
            }


            binding.root.setOnClickListener {
                onItemClick(program)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CalendarProgramResponse>() {
            override fun areItemsTheSame(oldItem: CalendarProgramResponse, newItem: CalendarProgramResponse): Boolean {
                return oldItem.id == newItem.id // Gunakan ID unik untuk membandingkan item
            }

            override fun areContentsTheSame(oldItem: CalendarProgramResponse, newItem: CalendarProgramResponse): Boolean {
                return oldItem == newItem // Bandingkan seluruh konten data class
            }
        }
    }
}