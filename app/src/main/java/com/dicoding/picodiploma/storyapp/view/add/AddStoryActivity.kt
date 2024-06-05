package com.dicoding.picodiploma.storyapp.view.add

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.picodiploma.storyapp.view.ViewModelFactory
import com.dicoding.picodiploma.storyapp.view.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding

    private val viewModel by viewModels<AddStoryViewModel>{
        ViewModelFactory.getInstance(this)
    }

    private var currentImageUri: Uri? = null
    private val MAXIMAL_SIZE = 1000000

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        binding.galleryButton.setOnClickListener {
            selectImageFromGallery()
        }

        binding.cameraButton.setOnClickListener {
            captureImageFromCamera()
        }

        binding.buttonAdd.setOnClickListener {
            uploadStory()
        }

        viewModel.addStoryResult.observe(this) { response ->
            if (response.error == false) {
                Toast.makeText(this, "Story uploaded!", Toast.LENGTH_SHORT).show()
            }  else {
                Toast.makeText(this, "Failed to upload story!", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) {isLoading -> binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE }
    }

    private fun uploadStory() {
        val description = binding.descLayout.editText?.text.toString().toRequestBody("text/plain".toMediaType())

        val file = currentImageUri?.let { uri ->
            val tempFile = getFileFromUri(uri)?.reduceFileImage()
            tempFile?.let {
                val requestFile = it.asRequestBody("image/jpeg".toMediaType())
                MultipartBody.Part.createFormData("photo", it.name, requestFile)
            }
        }

        if (file != null) {
            viewModel.addStory(file, description)
        } else {
            Log.e("AddStoryActivity", "File is null")
        }

        viewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            binding.ivPreview.setImageURI(uri)
        } else {
            Log.d("Gallery", "No media selected")
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            currentImageUri = getImageUriFromBitmap(imageBitmap)
            binding.ivPreview.setImageURI(currentImageUri)
        }
    }

    private fun selectImageFromGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun captureImageFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    fun File.reduceFileImage(): File {
        val file = this
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > MAXIMAL_SIZE)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("temp", null, cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = android.Manifest.permission.CAMERA
    }
}