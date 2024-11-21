package com.example.nexas

import android.Manifest
import android.content.ContentValues
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrivacyVideoFragment : Fragment() {

    private val model: ViewModel by activityViewModels()

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var audioFile: File
    private lateinit var videoFile: File
    private var isRecording = false
    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString("groupId") ?: ""
        }
        if (model.findMyGroupById(groupId) == null) {
            Toast.makeText(context, "Error: Group not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_privacy_video, container, false)

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            1
        )

        audioFile = File(requireContext().filesDir, "recorded_audio.aac")
        videoFile = createUniqueVideoFile()

        val backButton: ImageButton = view.findViewById(R.id.backButton)
        val videoCaptureButton: ShapeableImageView = view.findViewById(R.id.videoCaptureButton)

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        videoCaptureButton.setOnClickListener {
            if (isRecording) {
                stopAudioRecording()
                videoCaptureButton.apply {
                    setColorFilter(resources.getColor(R.color.mint))
                }
                createBlackScreenVideo()
            } else {
                startAudioRecording()
                videoCaptureButton.apply {
                    setColorFilter(resources.getColor(R.color.red))
                }
            }
            isRecording = !isRecording
        }

        return view
    }

    private fun startAudioRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile.absolutePath)
            prepare()
            start()
        }
    }

    private fun stopAudioRecording() {
        mediaRecorder.stop()
        mediaRecorder.release()
    }

    private fun createBlackScreenVideo() {
        val tempFile = File(requireContext().cacheDir, "temp_video.mp4")

        val command =
            "-f lavfi -i color=color=black:size=1920x1080:rate=30 -i ${audioFile.absolutePath} -c:v mpeg4 -c:a aac -shortest ${tempFile.absolutePath}"

        FFmpegKit.executeAsync(command) { session ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                Log.i("FFmpeg", "Video created successfully at: ${tempFile.absolutePath}")

                saveVideoToMediaStore(tempFile)
            } else {
                Log.e("FFmpeg", "Failed to create video. Return code: $returnCode")
            }
        }
    }

    private fun saveVideoToMediaStore(tempFile: File) {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val contentResolver = requireContext().contentResolver
        val videoUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (videoUri == null) {
            Log.e("PrivacyVideoFragment", "Failed to create MediaStore entry.")
            Toast.makeText(context, "Error saving video", Toast.LENGTH_SHORT).show()
            return
        }

        contentResolver.openOutputStream(videoUri)?.use { outputStream ->
            tempFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        tempFile.delete()

        val action = PrivacyVideoFragmentDirections.actionPrivacyVideoFragmentToPreviewFragment(
            groupId,
            videoUri.toString()
        )
        findNavController().navigate(action)
    }

    private fun createUniqueVideoFile(): File {
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(Date())
        val fileName = "$timestamp.mp4"
        return File(requireContext().filesDir, fileName)
    }
}
