package com.formichelli.dfreminder

import android.content.Context
import android.content.res.TypedArray
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.TimePicker

class TimePreference(ctxt: Context, attrs: AttributeSet) : DialogPreference(ctxt, attrs) {
    private var lastHour = 0
    private var lastMinute = 0
    private var picker: TimePicker? = null

    init {
        positiveButtonText = "Set"
        negativeButtonText = "Cancel"
    }

    override fun setDialogTitle(dialogTitle: CharSequence) {
        super.setDialogTitle("")
    }

    override fun onCreateDialogView(): View? {
        picker = TimePicker(context)

        return picker
    }

    override fun onBindDialogView(v: View) {
        super.onBindDialogView(v)

        picker!!.currentHour = lastHour
        picker!!.currentMinute = lastMinute
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)

        if (positiveResult) {
            lastHour = picker!!.currentHour
            lastMinute = picker!!.currentMinute

            val time = String.format("%02d", lastHour) + ":" + String.format("%02d", lastMinute)

            summary = time

            if (callChangeListener(time)) {
                persistString(time)
            }
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int) = a.getString(index)

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        val time: String
        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00")
            } else {
                time = getPersistedString(defaultValue.toString())
            }
        } else {
            time = defaultValue!!.toString()
        }

        summary = time
        lastHour = getHour(time)
        lastMinute = getMinute(time)
    }

    companion object {
        fun getHour(time: String) = time.split(":")[0].toInt()

        fun getMinute(time: String) = time.split(":")[1].toInt()
    }
}