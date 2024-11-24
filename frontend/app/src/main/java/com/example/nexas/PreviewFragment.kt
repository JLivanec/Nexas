package com.example.nexas

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentPreviewBinding
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PreviewFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var backButton: ImageButton
    private lateinit var playButton: ShapeableImageView
    private lateinit var sendButton: Button
    private lateinit var thumbnailImage: ImageView
    private lateinit var videoView: VideoView

    private lateinit var groupId: String
    private lateinit var videoURI: String
    private lateinit var videoImageURI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString("groupId") ?: ""
            videoURI = it.getString("videoURI") ?: ""
        }
        if (model.findMyGroupById(groupId) == null) {
            Toast.makeText(context, "Error: Group not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        val view = binding.root

        backButton = binding.backButton
        playButton = binding.playButton
        sendButton = binding.sendButton
        thumbnailImage = binding.thumbnailImage
        videoView = binding.videoView

        backButton.setOnClickListener(this)
        playButton.setOnClickListener(this)
        sendButton.setOnClickListener(this)

        loadThumbnail()

        return view
    }

    private fun loadThumbnail() {
        if (videoURI.isNotEmpty()) {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, Uri.parse(videoURI))
            val bitmap = mediaMetadataRetriever.getFrameAtTime()

            if (bitmap != null) {
                // Save the bitmap to a file and get the URI
                videoImageURI = saveBitmapToFile(bitmap)
                thumbnailImage.setImageBitmap(bitmap)
            } else {
                Toast.makeText(context, "Error: Could not retrieve thumbnail", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Error: Video URI is empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
        val file = File(context?.cacheDir, "thumbnail_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return Uri.fromFile(file) // Return the URI of the saved file
    }

    // Handles onClick Events
    override fun onClick(v: View?) {
        when (v?.id) {
            backButton.id -> {
                findNavController().navigateUp()
            }
            playButton.id -> {
                playVideo()
            }
            sendButton.id -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    sendButton.isEnabled = false
                    model.sendVideo(videoURI, videoImageURI.toString(), groupId)
                    findNavController().navigate(
                        PreviewFragmentDirections.actionPreviewFragmentToChatFragment(groupId)
                    )
                }
            }
        }
    }

    private fun playVideo() {
        if (videoURI.isNotEmpty()) {
            videoView.setVideoURI(Uri.parse(videoURI))
            videoView.setZOrderMediaOverlay(true)
            videoView.setOnPreparedListener { mediaPlayer: MediaPlayer ->
                mediaPlayer.isLooping = false
                videoView.start()
                playButton.visibility = View.GONE
                thumbnailImage.visibility = View.GONE
            }

            videoView.setOnCompletionListener {
                videoView.stopPlayback()
                videoView.seekTo(0)
                playButton.visibility = View.VISIBLE
                thumbnailImage.visibility = View.VISIBLE
            }

            videoView.setOnErrorListener { _, _, _ ->
                Toast.makeText(context, "Error playing video", Toast.LENGTH_SHORT).show()
                playButton.visibility = View.VISIBLE // Show play button on error
                thumbnailImage.visibility = View.VISIBLE
                true // Indicate the error was handled
            }
        } else {
            Toast.makeText(context, "Error: Video URI is empty", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
