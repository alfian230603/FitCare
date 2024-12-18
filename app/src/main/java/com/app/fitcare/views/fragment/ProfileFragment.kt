package com.app.fitcare.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.fitcare.databinding.FragmentProfileBinding
import com.app.fitcare.models.User
import com.app.fitcare.repositories.UserRepository
import com.app.fitcare.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository()
        sessionManager = SessionManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.buttonSave.setOnClickListener { saveProfileData() }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        val userId = sessionManager.getUser()?.first ?: run {
            Toast.makeText(requireContext(), "Session tidak ditemukan", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        userRepository.readById(userId, { userPair ->
            userPair?.let { (_, user) ->
                bindUserData(user)
            } ?: Toast.makeText(requireContext(), "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
        }, {
            Toast.makeText(requireContext(), "Gagal memuat data pengguna", Toast.LENGTH_SHORT).show()
        })
    }

    private fun bindUserData(user: User) {
        binding.apply {
            editName.setText(user.name)
            editUsername.setText(user.username)
            editEmail.setText(user.email)
            editPhone.setText(user.phone)
            editPassword.setText(user.password)
            editTargetWater.setText(user.targetWater.toString())
            editTargetSteps.setText(user.targetSteps.toString())
        }
    }

    private fun saveProfileData() {
        val userId = sessionManager.getUser()?.first ?: return

        val updatedUser = mapOf(
            "name" to binding.editName.text.toString(),
            "username" to binding.editUsername.text.toString(),
            "email" to binding.editEmail.text.toString(),
            "phone" to binding.editPhone.text.toString(),
            "password" to binding.editPassword.text.toString(),
            "targetWater" to (binding.editTargetWater.text.toString().toIntOrNull() ?: 2000),
            "targetSteps" to (binding.editTargetSteps.text.toString().toIntOrNull() ?: 5000)
        )

        userRepository.update(userId, updatedUser, {
            Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
        }, {
            Toast.makeText(requireContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}