package com.codecx.videodownloader.features.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.models.MediaItem
import com.codecx.videodownloader.data.states.ResultState
import com.codecx.videodownloader.data.utils.showToast
import com.codecx.videodownloader.databinding.DownloadBottomSheetFragmentBinding
import com.codecx.videodownloader.features.adapters.DownloadOptionAdapter
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Collections.emptyList

class DownloadOptionBottomSheet : BottomSheetDialogFragment() {
    private var _binding: DownloadBottomSheetFragmentBinding? = null
    private val binding get() = _binding!!
    private val downloadViewModel: DownloadViewModel by activityViewModel()


    companion object {
        const val TAG = "DownloadOptionsBottomSheet"
        fun newInstance(): DownloadOptionBottomSheet {
            return DownloadOptionBottomSheet()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        downloadViewModel.clearResult()
        super.onDismiss(dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DownloadBottomSheetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                downloadViewModel.downloadInfo.collectLatest { result ->
                    when (result) {
                        is ResultState.Success -> {
                            val data = result.data

                            Glide.with(binding.ivThumbnail)
                                .load(data.thumbnail)
                                .placeholder(R.drawable.ic_video_placeholder)
                                .centerCrop()
                                .into(binding.ivThumbnail)

                            // ── Title & author ────────────────────────────────────────────────────
                            binding.tvTitle.text = data.title ?: getString(R.string.app_name)
                            binding.tvAuthor.text = "@${data.author ?: ""}"

                            // ── Close ─────────────────────────────────────────────────────────────
                            binding.ivClose.setOnClickListener { dismiss() }

                            // ── Filter valid media items ──────────────────────────────────────────
                            val mediaItems =
                                data.medias?.filter { !it.url.isNullOrEmpty() } ?: emptyList()

                            if (mediaItems.isEmpty()) {
                                requireContext().showToast(getString(R.string.not_available))
                                dismiss()

                            } else {
                                val adapter =
                                    DownloadOptionAdapter(mediaItems, data) { selectedItem ->
                                        startDownload(
                                            title = data.title ?: "video",
                                            thumbnail = data.thumbnail ?: "",
                                            item = selectedItem
                                        )
                                    }
                                binding.rvDownloadOptions.layoutManager =
                                    LinearLayoutManager(requireContext())
                                binding.rvDownloadOptions.adapter = adapter
                            }


                        }

                        else -> {}
                    }
                }
            }
        }

    }

    private fun startDownload(title: String, thumbnail: String, item: MediaItem) {
        val downloadItem = DownloadModel(
            title = title,
            url = item.url ?: return,
            thumbnailUrl = thumbnail,
            fileSize = item.formattedSize(),
            isAudio = item.isAudio()
        )
        downloadViewModel.startDownload(downloadItem)
        requireContext().showToast(
            getString(R.string.download_started)
        )
        dismiss()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}