package com.acetylsalicylsaeure.platten.ui.components

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.Date

@RunWith(AndroidJUnit4::class)
class WeightProgressChartTest {

    private lateinit var testLogs: List<Quadruple<Int, Float, Int, Date>>

    private fun createDate(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.time
    }

    @Before
    fun setup() {
        // Create test data deliberately out of chronological order
        testLogs = listOf(
            Quadruple(1, 100f, 5, createDate(3)),   // 3 days ago
            Quadruple(1, 102.5f, 5, createDate(1)), // yesterday
            Quadruple(1, 97.5f, 5, createDate(4)),  // 4 days ago
            Quadruple(1, 105f, 5, createDate(0)),   // today
            Quadruple(1, 100f, 5, createDate(2))    // 2 days ago
        )
    }

    @Test
    fun testDateBasedSorting() {
        // Sort logs by date
        val sortedLogs = testLogs.sortedBy { it.fourth }

        // Verify logs are in chronological order
        for (i in 0 until sortedLogs.size - 1) {
            assertTrue(
                "Each log should be from an earlier or same date as the next log",
                sortedLogs[i].fourth.time <= sortedLogs[i + 1].fourth.time
            )
        }

        // Verify the weights are in the expected chronological order after sorting
        val expectedWeights = listOf(97.5f, 100f, 100f, 102.5f, 105f)
        val actualWeights = sortedLogs.map { it.second }
        assertEquals(
            "Weights should be in chronological order after date sorting",
            expectedWeights,
            actualWeights
        )
    }

    @Test
    fun testViewWindowWithDateOrder() {
        val viewWindow = 3
        val sortedLogs = testLogs.sortedBy { it.fourth }
        val filteredLogs = if (viewWindow > 0 && viewWindow < sortedLogs.size) {
            sortedLogs.takeLast(viewWindow)
        } else {
            sortedLogs
        }

        // Verify we get the most recent logs
        assertEquals("Should return exactly viewWindow logs", viewWindow, filteredLogs.size)

        // Verify the filtered logs are the most recent ones in correct order
        val expectedWeights = listOf(100f, 102.5f, 105f)  // Most recent 3 weights
        val actualWeights = filteredLogs.map { it.second }
        assertEquals(
            "Filtered weights should be the most recent ones in chronological order",
            expectedWeights,
            actualWeights
        )

        // Verify the dates are still in order
        for (i in 0 until filteredLogs.size - 1) {
            assertTrue(
                "Filtered logs should maintain chronological order",
                filteredLogs[i].fourth.time <= filteredLogs[i + 1].fourth.time
            )
        }
    }

    @Test
    fun testModifiedDatesHandling() {
        // Create logs with deliberately modified dates
        val logsWithModifiedDates = listOf(
            Quadruple(1, 100f, 5, createDate(5)),  // Original log
            Quadruple(1, 102.5f, 5, createDate(2)), // Modified to be earlier
            Quadruple(1, 105f, 5, createDate(3))   // Modified to be in between
        )

        val sortedLogs = logsWithModifiedDates.sortedBy { it.fourth }

        // Verify correct ordering regardless of modification order
        // Dates are: 2 days ago (102.5f), 3 days ago (105f), 5 days ago (100f)
        val expectedWeights = listOf(100f, 105f, 102.5f)
        val actualWeights = sortedLogs.map { it.second }
        assertEquals(
            "Logs should be ordered by date regardless of modification history",
            expectedWeights,
            actualWeights
        )

        // Verify chronological ordering
        for (i in 0 until sortedLogs.size - 1) {
            assertTrue(
                "Modified dates should still maintain chronological order",
                sortedLogs[i].fourth.time <= sortedLogs[i + 1].fourth.time
            )
        }
    }
}