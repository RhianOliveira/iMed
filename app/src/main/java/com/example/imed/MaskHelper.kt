package com.example.imed

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

class MaskHelper {
    companion object {

        fun mask(editText: TextInputEditText, mask: String) {
            editText.addTextChangedListener(object : TextWatcher {
                var isUpdating = false
                var oldString = ""

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val str = s.toString().replace("[^0-9]".toRegex(), "")
                    var mascara = ""
                    if (isUpdating) {
                        oldString = str
                        isUpdating = false
                        return
                    }
                    var i = 0
                    for (m in mask.toCharArray()) {
                        if (m != '#' && str.length > oldString.length) {
                            mascara += m
                            continue
                        }
                        try {
                            mascara += str[i]
                        } catch (e: Exception) {
                            break
                        }
                        i++
                    }
                    isUpdating = true
                    editText.setText(mascara)
                    editText.setSelection(mascara.length)
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}