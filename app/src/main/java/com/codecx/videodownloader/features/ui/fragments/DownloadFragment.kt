package com.codecx.videodownloader.features.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.databinding.FragmentDownloadBinding
import com.codecx.videodownloader.features.adapters.DownloadFragmentAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class DownloadFragment : BaseFragment<FragmentDownloadBinding>() {

    private lateinit var activeAdapter : DownloadFragmentAdapter
    private lateinit var completedAdapter : DownloadFragmentAdapter

    private val viewModel: DownloadViewModel by activityViewModel()
    private var showingActive = true
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentDownloadBinding {
        return FragmentDownloadBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupTabListeners()
        observeViewModel()
        checkPermissionsAndLoad()

    }

    private fun setupAdapters() {
        activeAdapter    = DownloadFragmentAdapter(isCompleted = false, requireContext())
        completedAdapter = DownloadFragmentAdapter(isCompleted = true,  requireContext())
        binding.rvDownload.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDownload.adapter = activeAdapter
    }

    private fun setupTabListeners() {

        binding.inProgressButton.setOnClickListener {
            showingActive = true
            binding.rvDownload.adapter = activeAdapter
            updateEmptyState()
        }
        binding.downloadCompleteButton.setOnClickListener {
            showingActive = false
            binding.rvDownload.adapter = completedAdapter
            updateEmptyState()
        }
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {


                launch {
                    viewModel.activeDownloads.collectLatest { activeList ->
                        activeAdapter.submitList(activeList)
                        if (showingActive) updateEmptyState()
                    }
                }

                launch {
                    viewModel.completedDownloads.collectLatest { completedList ->
                        completedAdapter.submitList(completedList)
                        if (!showingActive) updateEmptyState()
                    }
                }
            }
        }

    }

    private fun updateEmptyState() {
        val isEmpty = if (showingActive) {
            activeAdapter.itemCount == 0
        } else {
            completedAdapter.itemCount == 0
        }

        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvDownload.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun checkPermissionsAndLoad() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) == PackageManager.PERMISSION_GRANTED
        }


        if (allGranted) viewModel.refreshDownloads()
        else permissionLauncher.launch(permissions)
    }
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.refreshDownloads()
        }
    }



}