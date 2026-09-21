package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.InterestModel
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentInterestBinding
import com.codecx.videodownloader.features.adapters.InterestAdapter
import com.codecx.videodownloader.features.base.BaseFragment

class InterestFragment : BaseFragment<FragmentInterestBinding>() {
    private lateinit var adapter : InterestAdapter
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentInterestBinding {
        return FragmentInterestBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        val interests = listOf(
            InterestModel(getString(R.string.movies), R.drawable.moviews_icon),
            InterestModel(getString(R.string.news), R.drawable.news_icon),
            InterestModel(getString(R.string.fashion), R.drawable.fashion_icon),
            InterestModel(getString(R.string.bollywood), R.drawable.bollywood_icon),
            InterestModel(getString(R.string.technology), R.drawable.tech_unbox_icon),
            InterestModel(getString(R.string.gaming), R.drawable.gaming_icon),
            InterestModel(getString(R.string.music), R.drawable.music_icon),
            InterestModel(getString(R.string.sports), R.drawable.sport_icon),
            InterestModel(getString(R.string.entertainment), R.drawable.moviews_icon),
            InterestModel(getString(R.string.tech_unboxing), R.drawable.tech_unbox_icon),
            InterestModel(getString(R.string.education), R.drawable.education_icon),
            InterestModel(getString(R.string.health_fitness), R.drawable.health_icon)
        )
        adapter = InterestAdapter(interests)
        binding.rvInterests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInterests.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            if (adapter.getSelectedItems().isEmpty()){
                Toast.makeText(requireContext(),
                    getString(R.string.please_select_at_least_one_interest),Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                safeNavigate(R.id.languageFragment)
            }

        }
    }
}