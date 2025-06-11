package com.example.gnu_android_programming.reservation

import android.text.Editable
import android.text.TextWatcher

abstract class SimpleTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable?) {}
    abstract override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
}