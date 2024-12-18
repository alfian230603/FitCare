package com.app.fitcare.views.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.fitcare.R
import com.app.fitcare.databinding.FragmentHomeBinding
import com.app.fitcare.models.DailyLog
import com.app.fitcare.models.StepLog
import com.app.fitcare.models.WaterLog
import com.app.fitcare.repositories.StepLogRepository
import com.app.fitcare.repositories.WaterLogRepository
import com.app.fitcare.repositories.UserRepository
import com.app.fitcare.utils.SessionManager
import com.app.fitcare.views.HomeAdapter
import com.app.fitcare.views.LoginActivity
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var homeAdapter: HomeAdapter
    private lateinit var userRepository: UserRepository

    private val waterLogRepository = WaterLogRepository()
    private val stepLogRepository = StepLogRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        sessionManager = SessionManager(requireContext())
        userRepository = UserRepository()

        val user = sessionManager.getUser()
        if (user == null) {
            Toast.makeText(requireContext(), "Session tidak ditemukan", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        val (userId, _) = user

        homeAdapter = HomeAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
        }

        binding.fabAdd.setOnClickListener {
            showAddWaterLogDialog(userId)
        }

        saveOrUpdateStepsInFirestore(userId)
        loadLogs(userId)
        loadUserTargetWater(userId)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.clearSession()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveOrUpdateStepsInFirestore(userId: String) {
        val today = getCurrentDate()
        val sessionSteps = sessionManager.getDailySteps(today)

        if (sessionSteps > 0) {
            stepLogRepository.readByUserId(userId, { logs ->
                val existingLog = logs.find { it.second.date == today }

                if (existingLog != null) {
                    val (logId, _) = existingLog
                    val updatedData = mapOf("stepCount" to sessionSteps)
                    stepLogRepository.update(logId, updatedData, {
                        Log.d("HomeFragment", "Langkah diperbarui: $sessionSteps pada tanggal $today.")
                    }, {
                        Log.e("HomeFragment", "Gagal memperbarui langkah kaki.")
                    })
                } else {
                    val stepLog = StepLog(userId, today, sessionSteps)
                    stepLogRepository.create(stepLog, { docId ->
                        Log.d("HomeFragment", "Langkah disimpan dengan ID: $docId, Langkah: $sessionSteps pada tanggal: $today")
                    }, {
                        Log.e("HomeFragment", "Gagal menyimpan langkah kaki ke Firestore.")
                    })
                }
            }, {
                Log.e("HomeFragment", "Gagal memeriksa data langkah kaki di Firestore.")
            })
        } else {
            Log.d("HomeFragment", "Langkah dari session kosong untuk $today.")
        }
    }

    private fun showAddWaterLogDialog(userId: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_water_log, null)
        val editWaterIntake = dialogView.findViewById<EditText>(R.id.editWaterIntake)

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Data Air")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val waterIntake = editWaterIntake.text.toString().toIntOrNull()
                if (waterIntake != null && waterIntake > 0) {
                    val currentDate = getCurrentDate()
                    val waterLog = WaterLog(userId, currentDate, waterIntake)
                    waterLogRepository.create(waterLog, {
                        Toast.makeText(requireContext(), "Data air berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        loadLogs(userId)
                    }, {
                        Toast.makeText(requireContext(), "Gagal menambahkan data air", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    Toast.makeText(requireContext(), "Masukkan jumlah air yang valid", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadLogs(userId: String) {
        waterLogRepository.readByUserId(userId, { waterLogs ->
            stepLogRepository.readByUserId(userId, { stepLogs ->
                val mergedData = mergeLogsByDate(
                    waterLogs.map { it.second },
                    stepLogs.map { it.second }
                )
                updateDailyAchievement(waterLogs.sumOf { it.second.waterIntake })
                homeAdapter.setData(mergedData)
            }, {
                Toast.makeText(requireContext(), "Gagal memuat data langkah", Toast.LENGTH_SHORT).show()
            })
        }, {
            Toast.makeText(requireContext(), "Gagal memuat data air", Toast.LENGTH_SHORT).show()
        })
    }

    private fun loadUserTargetWater(userId: String) {
        userRepository.readById(userId, { userPair ->
            userPair?.let { (_, user) ->
                binding.tvTargetWater.text = "Target Harian: ${user.targetWater} ml"
                Log.d("HomeFragment", "Target Water Loaded: ${user.targetWater}")
            } ?: run {
                Toast.makeText(requireContext(), "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }, {
            Toast.makeText(requireContext(), "Gagal memuat target pengguna", Toast.LENGTH_SHORT).show()
        })
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun updateDailyAchievement(totalWaterIntake: Int) {
        binding.tvDailyAchievement.text = "Pencapaian Minum Hari Ini: ${totalWaterIntake} ml"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
