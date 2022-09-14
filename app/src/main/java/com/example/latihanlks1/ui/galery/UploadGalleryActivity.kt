package com.example.latihanlks1.ui.galery

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.latihanlks1.R
import com.example.latihanlks1.data.model.Album
import com.example.latihanlks1.data.network.NetworkApi
import com.example.latihanlks1.databinding.ActivityUploadGalleryBinding
import com.example.latihanlks1.util.getRealPathFromURI
import com.example.latihanlks1.util.loadBitmap
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File

class UploadGalleryActivity : AppCompatActivity() {
    lateinit var binding : ActivityUploadGalleryBinding
    lateinit var file: File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadGalleryBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setUp()
    }

    private fun setUp(){
        binding.btnUpload.setOnClickListener {
            storagePermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))

        }
    }

    private fun bind(imgurl : String){
        CoroutineScope(Dispatchers.Main).launch {
            binding.prBar.visibility = View.VISIBLE
                val bitmap = withContext(Dispatchers.IO){
                    imgurl?.let {
                        loadBitmap(it)
                    }
                }
                bitmap?.let {
                    binding.ivImageupload.setImageBitmap(it)
                    binding.prBar.visibility = View.INVISIBLE
                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Log.d("ActivityResultGallery", it.data?.data.toString())
                it.data?.data?.let { uri ->
                    getRealPathFromURI(uri)?.let { realPath ->
                        Log.d("getRealPathFromURI", realPath)
                        file = File(realPath)
                        CoroutineScope(Dispatchers.Main).launch {
                            postImage(file)?.let {
                                val imgurl = it.getString("thumb")
                                bind(imgurl)
                                Log.d("postImage", realPath)
                            }
                        }
                    }
                }
            }
        }

    private val storagePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true -> {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf("image/jpg", "image/jpeg", "image/png")
                    )
                    galleryLauncher.launch(
                        intent
                    )
                }
            }
        }

    private suspend fun postImage(file: File): JSONObject? {
        return try {
            withContext(Dispatchers.IO) {
                NetworkApi(
                    "https://thumbsnap.com/api/upload",
                    method = "POST",
//                    headers = arrayOf(
//                        Pair("x-api-key", "live_F9QVdZ5GmaliK9O0lugJmvTckMYhL74IrOll5JGyPA2UchBKC5CvPCD7s0lUBb7d"),
//                    ),
                ).addFormField("key", "00001f67db9714503fe34ba6bc05dea8")
                    .addFilePart("media", file)
                    .execute()?.let {
                        JSONObject(it)
                            .getJSONObject("data")
                    }
            }

        } catch (e: Exception) {
            Toast.makeText(this@UploadGalleryActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            null
        }
    }
}