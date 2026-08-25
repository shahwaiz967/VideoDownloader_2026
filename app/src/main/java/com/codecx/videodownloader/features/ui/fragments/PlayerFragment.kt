package com.codecx.videodownloader.features.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.data.utils.showToast
import java.io.File
import androidx.appcompat.app.AlertDialog
import com.codecx.videodownloader.data.models.MediaModel
import com.codecx.videodownloader.features.ui.viewmodel.PlayerViewModel
import com.codecx.videodownloader.databinding.FragmentPlayerBinding
import com.codecx.videodownloader.features.adapters.PlayerChipAdapter
import com.codecx.videodownloader.features.adapters.PlayerMediaAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : BaseFragment<FragmentPlayerBinding>() {
    private lateinit var chipAdapter: PlayerChipAdapter
    private lateinit var mediaAdapter: PlayerMediaAdapter
    private var deleteLauncher: ActivityResultLauncher<IntentSenderRequest?>? = null

    private val viewModel: PlayerViewModel by viewModel()

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPlayerBinding {
        return FragmentPlayerBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMediaAdapter()
        setupFolderAdapter()
        observeData()
        setupClickListeners()

        checkPermissionsAndLoad()

        deleteLauncher = registerForActivityResult<IntentSenderRequest?, ActivityResult?>(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult? ->
            if (result!!.resultCode == Activity.RESULT_OK) {
               loadFoldersAndMedia(forceRefresh = true)
            }
        }
    }

    private fun setupClickListeners() {
        binding.allVideoButton.setOnClickListener {
            if (!viewModel.showingVideos) {
                viewModel.showingVideos = true
                viewModel.selectedFolder = "All"
                chipAdapter.setSelectedFolder("All")
                mediaAdapter.submitList(viewModel.videos.value.toList())
                chipAdapter.submitFolders(viewModel.videoFolders.value)
                binding.mediaRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
                binding.mediaRecycler.adapter = mediaAdapter
                loadFoldersAndMedia()
            }
        }

        binding.allMusicButton.setOnClickListener {
            if (viewModel.showingVideos) {
                viewModel.showingVideos = false
                viewModel.selectedFolder = "All"
                chipAdapter.setSelectedFolder("All")
                mediaAdapter.submitList(viewModel.audios.value.toList())
                chipAdapter.submitFolders(viewModel.audioFolders.value)
                binding.mediaRecycler.layoutManager = LinearLayoutManager(requireContext())
                binding.mediaRecycler.adapter = mediaAdapter
                loadFoldersAndMedia()
            }
        }
    }

    private fun setupMediaAdapter() {
        mediaAdapter = PlayerMediaAdapter(
            onItemClick = { media ->
                val bundle = Bundle()
                bundle.putString("videoPath", media.path)
                bundle.putString("videoTitle", media.title)
                bundle.putBoolean("isAudio", media.isAudio)
                safeNavigate(R.id.mediaPlayFragment, bundle)
            },
            onDeleteClick = { media ->
                performDelete(media)
            }
        )

        binding.mediaRecycler.apply {
            layoutManager = if (viewModel.showingVideos) {
                GridLayoutManager(requireContext(), 2)
            } else {
                LinearLayoutManager(requireContext())
            }
            adapter = mediaAdapter
        }
    }


    private fun performDelete(media: MediaModel) {
        viewModel.deleteMedia(
            context = requireContext(),
            media = media,
            onIntentSenderNeeded = { pendingIntent ->
                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                deleteLauncher?.launch(intentSenderRequest)
            },
            onSuccess = {
                loadFoldersAndMedia(forceRefresh = true)
                requireContext().showToast("Deleted successfully")
            }
        )
    }



    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    viewModel.videos.collect { videos ->
                        if (viewModel.showingVideos) {
                            mediaAdapter.submitList(videos.toList())
                        }
                    }
                }
                launch {
                    viewModel.audios.collect { audios ->
                        if (!viewModel.showingVideos) {
                            mediaAdapter.submitList(audios.toList())
                        }
                    }
                }
                launch {
                    viewModel.videoFolders.collect { folders ->
                        if (viewModel.showingVideos) {
                            chipAdapter.submitFolders(folders)
                        }
                    }
                }
                launch {
                    viewModel.audioFolders.collect { folders ->
                        if (!viewModel.showingVideos) {
                            chipAdapter.submitFolders(folders)
                        }
                    }
                }
            }
        }
    }

    private fun setupFolderAdapter() {
        chipAdapter = PlayerChipAdapter { folder ->
            viewModel.selectedFolder = folder
            if (viewModel.showingVideos) {
                viewModel.loadVideos(
                    requireContext(),
                    folder
                )
            } else {
                viewModel.loadAudio(
                    requireContext(),
                    folder
                )
            }
        }

        chipAdapter.setSelectedFolder(viewModel.selectedFolder)

        binding.chipRecycler.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.chipRecycler.adapter = chipAdapter
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

        if (allGranted) loadFoldersAndMedia()
        else permissionLauncher.launch(permissions)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            loadFoldersAndMedia()
        }
    }

    private fun loadFoldersAndMedia(forceRefresh: Boolean = false) {
        if (viewModel.showingVideos) {
            viewModel.loadVideoFolders(requireContext(), forceRefresh)
            viewModel.loadVideos(requireContext(), viewModel.selectedFolder, forceRefresh)
        } else {
            viewModel.loadAudioFolder(requireContext(), forceRefresh)
            viewModel.loadAudio(requireContext(), viewModel.selectedFolder, forceRefresh)
        }
    }

}