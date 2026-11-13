package com.musicinterval.ear.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.musicinterval.ear.core.theme.Error
import com.musicinterval.ear.core.theme.Success

@Composable
fun FeedbackMark(correct: Boolean) {
    Canvas(Modifier.size(24.dp)) {
        val stroke = 2.5.dp.toPx()
        if (correct) {
            drawLine(Success, Offset(size.width * 0.16f, size.height * 0.5f), Offset(size.width * 0.4f, size.height * 0.74f), stroke, StrokeCap.Round)
            drawLine(Success, Offset(size.width * 0.4f, size.height * 0.74f), Offset(size.width * 0.86f, size.height * 0.24f), stroke, StrokeCap.Round)
        } else {
            drawLine(Error, Offset(size.width * 0.24f, size.height * 0.24f), Offset(size.width * 0.76f, size.height * 0.76f), stroke, StrokeCap.Round)
            drawLine(Error, Offset(size.width * 0.76f, size.height * 0.24f), Offset(size.width * 0.24f, size.height * 0.76f), stroke, StrokeCap.Round)
        }
    }
}
