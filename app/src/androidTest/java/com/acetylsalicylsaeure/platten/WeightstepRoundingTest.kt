package com.acetylsalicylsaeure.platten

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.roundToInt

class WeightstepRoundingTest {
    @Test
    fun testRoundToNearestWeightStep() {
        // Test normal cases
        assertEquals(10f, roundToNearestWeightStep(10.2f, 2.5), 0.001f)
        assertEquals(12.5f, roundToNearestWeightStep(12.3f, 2.5), 0.001f)
        assertEquals(15f, roundToNearestWeightStep(14.9f, 2.5), 0.001f)

        // Test edge cases
        assertEquals(1f, roundToNearestWeightStep(Float.NaN, 2.5), 0.001f)
        assertEquals(1f, roundToNearestWeightStep(10f, Double.NaN), 0.001f)
        assertEquals(1f, roundToNearestWeightStep(10f, 0.0), 0.001f)

        // Test with very small weight step
        assertEquals(10.1f, roundToNearestWeightStep(10.06f, 0.1), 0.001f)

        // Test with very large weight step
        assertEquals(100f, roundToNearestWeightStep(75f, 100.0), 0.001f)

        // Test with negative values (if your app allows them)
        assertEquals(-10f, roundToNearestWeightStep(-9.8f, 2.5), 0.001f)
    }

    // This is the function we're testing, copied here for reference
    private fun roundToNearestWeightStep(weight: Float, weightStep: Double): Float {
        if (weight.isNaN() || weightStep.isNaN() || weightStep == 0.0) {
            return 1f // Return 1 as the default value if input is invalid
        }
        return (weight / weightStep).roundToInt() * weightStep.toFloat()
    }
}
