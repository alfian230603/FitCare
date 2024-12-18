package com.app.fitcare

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.*
import com.app.fitcare.databinding.ActivityMainBinding
import com.app.fitcare.utils.SessionManager
import com.app.fitcare.views.FragmentActivity
import com.app.fitcare.views.LoginActivity
import com.app.fitcare.workers.StepCounterWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scheduleStepWorker()
        checkSession()
    }

    private fun scheduleStepWorker() {
        val stepWorkRequest = PeriodicWorkRequestBuilder<StepCounterWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .addTag("StepCounterWorker")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "StepCounterWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            stepWorkRequest
        )
        WorkManager.getInstance(this)
            .getWorkInfosByTagLiveData("StepCounterWorker")
            .observe(this) { workInfo ->
                if (workInfo.isNotEmpty() && workInfo.first().state.isFinished) {
                    println("Worker selesai dengan status ${workInfo.first().state}.")
                }
            }
    }

    private fun checkSession() {
        val user = sessionManager.getUser()
        if (user != null) {
            startActivity(Intent(this, FragmentActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
