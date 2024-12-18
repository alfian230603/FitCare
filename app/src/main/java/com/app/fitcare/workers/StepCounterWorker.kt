package com.app.fitcare.workers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.fitcare.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class StepCounterWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepSensor: Sensor? = null
    private var initialSteps = -1
    private val sessionManager = SessionManager(context)

    override suspend fun doWork(): Result {
        Log.d("StepCounterWorker", "Worker dimulai.")

        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Log.e("StepCounterWorker", "Sensor tidak tersedia.")
            return Result.failure()
        }

        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        Thread.sleep(10000)
        sensorManager.unregisterListener(this)

        Log.d("StepCounterWorker", "Worker selesai.")
        return Result.success()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            if (initialSteps == -1) initialSteps = steps

            val today = getCurrentDate()
            val dailySteps = steps - initialSteps
            sessionManager.saveDailySteps(today, dailySteps)

            Log.d("StepCounterWorker", "Langkah hari ini: $dailySteps")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
