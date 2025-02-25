package com.acetylsalicylsaeure.platten.ui.components

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
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData
import java.util.Date
import kotlin.math.roundToInt

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun WeightProgressChart(
    logs: List<Quadruple<Int, Float, Int, Date>>,
    viewWindow: Int,
    regression: Triple<Double, Double, Double>?,
    fitToLastSession: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val sortedLogs = logs.sortedBy { it.fourth }  // Sort by date
    val filteredLogs = if (viewWindow > 0 && viewWindow < sortedLogs.size) {
        sortedLogs.takeLast(viewWindow)
    } else {
        sortedLogs
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                CombinedChart(context).apply {
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
                val entries = filteredLogs.mapIndexed { index, (_, weight, reps, _) ->
                    val estimatedOneRM = calculateEstimatedOneRM(weight, reps)
                    Entry(index.toFloat(), estimatedOneRM)
                }
                val scatterDataSet = ScatterDataSet(entries, "Estimated 1RM Progress").apply {
                    setDrawValues(false)
                    color = primaryColor
                    setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                    scatterShapeSize = 12f
                }
                val scatterData = ScatterData(scatterDataSet)

                // Create CombinedData
                val combinedData = CombinedData()
                combinedData.setData(scatterData)

                // Add linear regression if available
                regression?.let { (slope, intercept, adjustment) ->
                    val regressionEntries = entries.mapIndexed { index, _ ->
                        val y = slope * index + intercept
                        Entry(index.toFloat(), y.toFloat())
                    }

                    val regressionDataSet = LineDataSet(regressionEntries, "Regression").apply {
                        color = secondaryColor
                        lineWidth = 2f
                        setDrawCircles(false)
                        setDrawValues(false)
                        enableDashedLine(10f, 5f, 0f)
                    }

                    val lineData = LineData(regressionDataSet)
                    combinedData.setData(lineData)
                }

                // Set the combined data to the chart
                chart.data = combinedData

                // Add padding to X-axis
                val xPadding = 0.5f
                chart.xAxis.apply {
                    axisMinimum = -xPadding
                    axisMaximum = (entries.size - 1 + xPadding).toFloat()
                    labelCount = entries.size.coerceAtMost(5)
                }

                // Add padding to Y-axis
                val yMin = entries.minOfOrNull { it.y } ?: 0f
                val yMax = entries.maxOfOrNull { it.y } ?: 100f
                val yRange = yMax - yMin
                val yPadding = yRange * 0.1f
                chart.axisLeft.apply {
                    axisMinimum = yMin - yPadding
                    axisMaximum = yMax + yPadding
                }

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
            text = "Session             ",
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

fun adjustWeightForReps(oneRM: Float, targetReps: Int): Float {
    return oneRM * (37f - targetReps) / 36f
}

fun roundToNearestWeightStep(weight: Float, weightStep: Double): Float {
    if (weight.isNaN()) {
        return 0f // Return 1 as the default value if input is invalid
    }
    if (weightStep.isNaN() || weightStep == 0.0) {
        return weight
    }
    return (weight / weightStep).roundToInt() * weightStep.toFloat()
}