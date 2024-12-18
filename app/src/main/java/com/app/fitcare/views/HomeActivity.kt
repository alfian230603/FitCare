package com.app.fitcare.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.fitcare.R
import com.app.fitcare.databinding.ActivityHomeBinding
import com.app.fitcare.models.DailyLog
import com.app.fitcare.models.WaterLog
import com.app.fitcare.models.StepLog
import com.app.fitcare.repositories.StepLogRepository
import com.app.fitcare.repositories.WaterLogRepository
import com.app.fitcare.utils.SessionManager

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var homeAdapter: HomeAdapter

    private val waterLogRepository = WaterLogRepository()
    private val stepLogRepository = StepLogRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBar)

        sessionManager = SessionManager(this)
        val user = sessionManager.getUser()

        if (user == null) {
            Toast.makeText(this, "Session tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val (userId, _) = user

        homeAdapter = HomeAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = homeAdapter
        }

        loadLogs(userId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadLogs(userId: String) {
        waterLogRepository.readByUserId(userId, { waterLogs ->
            stepLogRepository.readByUserId(userId, { stepLogs ->
                val mergedData = mergeLogsByDate(
                    waterLogs.map { it.second },
                    stepLogs.map { it.second }
                )
                homeAdapter.setData(mergedData)
            }, {
                Toast.makeText(this, "Gagal memuat data langkah", Toast.LENGTH_SHORT).show()
            })
        }, {
            Toast.makeText(this, "Gagal memuat data air", Toast.LENGTH_SHORT).show()
        })
    }

    private fun mergeLogsByDate(waterLogs: List<WaterLog>, stepLogs: List<StepLog>): List<DailyLog> {
        val logsMap = mutableMapOf<String, DailyLog>()

        waterLogs.forEach { log ->
            val date = log.date
            val dailyLog = logsMap.getOrPut(date) { DailyLog(date, 0, 0) }
            dailyLog.waterIntake += log.waterIntake
        }

        stepLogs.forEach { log ->
            val date = log.date
            val dailyLog = logsMap.getOrPut(date) { DailyLog(date, 0, 0) }
            dailyLog.stepCount += log.stepCount
        }

        return logsMap.values.sortedByDescending { it.date }
    }
}
