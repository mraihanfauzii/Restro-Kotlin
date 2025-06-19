package com.mraihanfauzii.restrokotlin.adapter.detect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mraihanfauzii.restrokotlin.databinding.ItemActionBinding

class ActionAdapter(
    private val data: List<String>,
    private val onAdd: (String) -> Unit
) : RecyclerView.Adapter<ActionAdapter.VH>() {

    inner class VH(val b: ItemActionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(name: String) = with(b) {
            tvName.text = name
            btnAdd.setOnClickListener { onAdd(name) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(
        ItemActionBinding.inflate(LayoutInflater.from(p.context), p, false)
    )
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(data[i])
    override fun getItemCount() = data.size
}
