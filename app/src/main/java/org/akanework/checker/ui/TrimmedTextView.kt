package org.akanework.checker.ui

import android.content.Context
import android.text.Layout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.ceil
import kotlin.math.max

class TrimmedTextView(
    context: Context,
    attrs: AttributeSet
) : AppCompatTextView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var maxWidth = ceil(getMaxLineWidth(layout)).toInt()
        maxWidth += paddingRight + paddingLeft

        setMeasuredDimension(maxWidth, measuredHeight)
    }

    private fun getMaxLineWidth(layout: Layout): Float {
        var maximumWidth = 0.0f
        val lines = layout.lineCount
        for (i in 0 until lines) {
            maximumWidth = max(layout.getLineWidth(i), maximumWidth)
        }

        return maximumWidth
    }
}