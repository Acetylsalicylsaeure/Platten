package com.acetylsalicylsaeure.platten

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.acetylsalicylsaeure.platten.data.ExerciseLog
import com.acetylsalicylsaeure.platten.ui.components.calculateEstimatedOneRM
import com.acetylsalicylsaeure.platten.viewmodel.ExerciseViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.math.round
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ExerciseViewModelTest {

    private lateinit var viewModel: ExerciseViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = ExerciseViewModel(application)
    }

    @Test
    fun test1RM() {
        val oneRM = calculateEstimatedOneRM(100f, 1)
        assertEquals(100.0, oneRM.toDouble(), 0.01, "1RM estimation for 100kg x 1 should be about 102.86kg")

        val fiveRM = calculateEstimatedOneRM(100f, 5)
        assertEquals(112.5, fiveRM.toDouble(), 0.01, "1RM estimation for 100kg x 5 should be about 116.28kg")
    }

    @Test
    fun testRegressionWith123kg() {
        val logs = listOf(
            ExerciseLog(exerciseId = 1, date = Date(), weight = 1f, reps = 1),
            ExerciseLog(exerciseId = 1, date = Date(), weight = 2f, reps = 1),
            ExerciseLog(exerciseId = 1, date = Date(), weight = 3f, reps = 1)
        )

        // Test with fit to last session = true
        val (slope, intercept, _) = viewModel.calculateLinearRegression(logs, false, 0, true)!!
        val prediction = slope * 3 + intercept
        assertEquals(4.0, prediction, 0.01, "Prediction should be 4 kg with fit to last session")

        // Test with fit to last session = false
        val (slopeNoFit, interceptNoFit, _) = viewModel.calculateLinearRegression(logs, false, 0, false)!!
        val predictionNoFit = slopeNoFit * 3 + interceptNoFit
        assertEquals(4.0, predictionNoFit, 0.01, "Prediction should be 4 kg without fit to last session")
    }

    @Test
    fun testRegressionWith1To14Then13kg() {
        val logs = (1..14).map {
            ExerciseLog(exerciseId = 1, date = Date(), weight = it.toFloat(), reps = 1)
        } + ExerciseLog(exerciseId = 1, date = Date(), weight = 13f, reps = 1)

        // Test with fit to last session = true
        val (slopeFit, interceptFit, _) = viewModel.calculateLinearRegression(logs, false, 0, true)!!
        val predictionFit = slopeFit * 15 + interceptFit
        val roundPredictionFit = round(predictionFit)
        assertEquals(14.0, roundPredictionFit, 0.05, "Prediction should be 14 kg with fit to last session")

        // Test with fit to last session = false
        val (slopeNoFit, interceptNoFit, _) = viewModel.calculateLinearRegression(logs, false, 0, false)!!
        val predictionNoFit = slopeNoFit * 15 + interceptNoFit
        assertEquals(15.0, round(predictionNoFit), 0.05, "Prediction should be 15 kg without fit to last session")
    }

    @Test
    fun testWeightedRegression() {
        val logs = listOf(
            ExerciseLog(exerciseId = 1, date = Date(), weight = 1f, reps = 1),
            ExerciseLog(exerciseId = 1, date = Date(), weight = 2f, reps = 1),
            ExerciseLog(exerciseId = 1, date = Date(), weight = 4f, reps = 1)
        )

        val (slopeWeighted, interceptWeighted, _) = viewModel.calculateLinearRegression(logs, true, 0, false)!!
        val predictionWeighted = slopeWeighted * 3 + interceptWeighted

        val (slopeUnweighted, interceptUnweighted, _) = viewModel.calculateLinearRegression(logs, false, 0, false)!!
        val predictionUnweighted = slopeUnweighted * 3 + interceptUnweighted

        assert(predictionWeighted > predictionUnweighted) {
            "Weighted regression should predict a higher value than unweighted for increasing weights"
        }
    }

    @Test
    fun testRegressionWindow() {
        val logs = (1..10).map {
            ExerciseLog(exerciseId = 1, date = Date(), weight = it.toFloat(), reps = 1)
        }

        val (slopeFull, interceptFull, _) = viewModel.calculateLinearRegression(logs, false, 0, false)!!
        val predictionFull = slopeFull * 10 + interceptFull

        val (slopeWindow, interceptWindow, _) = viewModel.calculateLinearRegression(logs, false, 5, false)!!
        val predictionWindow = slopeWindow * 10 + interceptWindow

        assert(predictionWindow > predictionFull) {
            "Regression with window should predict a higher value due to recent upward trend"
        }
    }
}