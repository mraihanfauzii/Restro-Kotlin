package com.mraihanfauzii.restrokotlin.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mraihanfauzii.restrokotlin.adapter.profile.AllBadgesAdapter
import com.mraihanfauzii.restrokotlin.adapter.profile.LeaderboardAdapter
import com.mraihanfauzii.restrokotlin.adapter.profile.MyBadgesAdapter
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.databinding.FragmentGamificationBinding
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.GamificationViewModel
import com.mraihanfauzii.restrokotlin.viewmodel.GamificationViewModelFactory

class GamificationFragment : Fragment() {

    private var _binding: FragmentGamificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var gamificationViewModel: GamificationViewModel
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var myBadgesAdapter: MyBadgesAdapter
    private lateinit var allBadgesAdapter: AllBadgesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authenticationManager = AuthenticationManager(requireContext())
        val apiService = ApiConfig.getApiService()
        val factory = GamificationViewModelFactory(apiService)
        gamificationViewModel = ViewModelProvider(this, factory)[GamificationViewModel::class.java]

        setupRecyclerViews()
        observeViewModel()
        fetchGamificationData()
    }

    private fun setupRecyclerViews() {
        leaderboardAdapter = LeaderboardAdapter()
        binding.rvLeaderboard.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = leaderboardAdapter
        }

        myBadgesAdapter = MyBadgesAdapter()
        binding.rvMyBadges.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = myBadgesAdapter
        }

        allBadgesAdapter = AllBadgesAdapter()
        binding.rvAllBadges.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = allBadgesAdapter
        }
    }

    private fun fetchGamificationData() {
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
        if (token != null) {
            gamificationViewModel.getLeaderboard(token)
            gamificationViewModel.getMyBadges(token)
            gamificationViewModel.getAllBadges(token)
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
            // Optionally navigate back to login or onboarding
        }
    }

    private fun observeViewModel() {
        gamificationViewModel.leaderboard.observe(viewLifecycleOwner) { leaderboard ->
            leaderboardAdapter.submitList(leaderboard)
            binding.tvNoLeaderboardData.visibility = if (leaderboard.isEmpty()) View.VISIBLE else View.GONE
        }

        gamificationViewModel.myBadges.observe(viewLifecycleOwner) { myBadges ->
            myBadgesAdapter.submitList(myBadges)
            binding.tvNoMyBadgesData.visibility = if (myBadges.isEmpty()) View.VISIBLE else View.GONE
        }

        gamificationViewModel.allBadges.observe(viewLifecycleOwner) { allBadges ->
            allBadgesAdapter.submitList(allBadges)
            binding.tvNoAllBadgesData.visibility = if (allBadges.isEmpty()) View.VISIBLE else View.GONE
        }

        gamificationViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        gamificationViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                gamificationViewModel.clearErrorMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}