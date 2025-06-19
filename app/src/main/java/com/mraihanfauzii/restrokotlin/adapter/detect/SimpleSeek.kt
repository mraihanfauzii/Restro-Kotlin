package com.mraihanfauzii.restrokotlin.adapter.detect

import android.widget.SeekBar

class SimpleSeek(private val onChange: (Int) -> Unit) : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { onChange(p) }
    override fun onStartTrackingTouch(sb: SeekBar?) = Unit
    override fun onStopTrackingTouch(sb: SeekBar?) = Unit
}
