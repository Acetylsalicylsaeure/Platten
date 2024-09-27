package com.example.platten.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet

@Composable
fun WeightProgressChart(logs: List<Triple<Int, Float, Int>>) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                ScatterChart(context).apply {
                    description.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(true)
                    setDrawGridBackground(false)

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        granularity = 1f
                        labelCount = 5
                        setTextColor(textColor)
                    }

                    axisLeft.apply {
                        setDrawGridLines(false)
                        setTextColor(textColor)
                        granularity = 1f
                        labelCount = 6
                        setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    }

                    axisRight.isEnabled = false
                    legend.isEnabled = false
                    setExtraOffsets(24f, 16f, 16f, 16f)

                }
            },
            update = { chart ->
                val entries = logs.mapIndexed { index, (_, weight, reps) ->
                    val estimatedOneRM = calculateEstimatedOneRM(weight, reps)
                    Entry(index.toFloat(), estimatedOneRM)
                }
                val dataSet = ScatterDataSet(entries, "Estimated 1RM Progress").apply {
                    setDrawValues(false)
                    color = primaryColor
                    setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                    scatterShapeSize = 12f
                }
                val scatterData = ScatterData(dataSet)
                chart.data = scatterData

                chart.invalidate()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, top = 16.dp, end = 8.dp, bottom = 24.dp)
        )

        Text(
            text = "Estimated 1RM",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .rotate(-90f)
                .offset(y = (-24).dp)
        )

        Text(
            text = "Session",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

fun calculateEstimatedOneRM(weight: Float, reps: Int): Float {
    return weight * (36f / (37f - reps))
}