package com.mraihanfauzii.restrokotlin.adapter.detect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mraihanfauzii.restrokotlin.databinding.ItemSelectedBinding
import com.mraihanfauzii.restrokotlin.model.ExercisePlan

class SelectedAdapter(
    private val onAdd:  (ExercisePlan) -> Unit,
    private val onSub:  (ExercisePlan) -> Unit,
    private val onDelete: (ExercisePlan) -> Unit
) : ListAdapter<ExercisePlan, SelectedAdapter.VH>(Diff()) {

    inner class VH(val b: ItemSelectedBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ExercisePlan) = with(b) {
            tvName.text  = item.actionName
            tvReps.text  = "${item.targetReps} repetisi"

            btnPlus.setOnClickListener   { onAdd(item) }
            btnMinus.setOnClickListener  { onSub(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemSelectedBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, i: Int) = h.bind(getItem(i))

    private class Diff : DiffUtil.ItemCallback<ExercisePlan>() {
        override fun areItemsTheSame(o: ExercisePlan, n: ExercisePlan) = o.actionName == n.actionName
        override fun areContentsTheSame(o: ExercisePlan, n: ExercisePlan) = o == n
    }
}