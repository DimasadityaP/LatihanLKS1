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
import com.example.latihanlks1.data.network.FormRequest
import com.example.latihanlks1.data.network.NetworkApi
import com.example.latihanlks1.databinding.ActivityUploadGalleryBinding
import com.example.latihanlks1.util.getRealPathFromURI
import com.example.latihanlks1.util.loadBitmap
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File

class UploadGalleryActivity : AppCompatActivity() {
    lateinit var binding : ActivityUploadGalleryBinding
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
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                Log.d("getRealPathFromURI", realPath)
                                val file = File(realPath)
                                withContext(Dispatchers.IO) {
                                    val requestForm = FormRequest()
                                        .addFormFile("media", file)
                                        .addFormField("key", "00001f67db9714503fe34ba6bc05dea8")

                                    val postImage = NetworkApi()
                                        .setMethod("POST")
                                        .setRequestURL("https://thumbsnap.com/api/upload")
                                        .addHeader("Content-Type", requestForm.contentType)
                                        .withBody(requestForm)
                                        .execute()
                                        .asJSON(JSONObject::class.java)

                                    if (postImage.httpStatusCode !in 200 until 300) {
                                        Toast.makeText(this@UploadGalleryActivity, "Error: ${postImage.rawResponseData}", Toast.LENGTH_LONG).show()
                                        return@withContext
                                    }

                                    Log.d("response", postImage.rawResponseData)
                                    bind(postImage.responseData.getJSONObject("data").getString("thumb"))
                                    Log.d("postImage", realPath)
                                }

                            } catch (e: Exception) {
                                Toast.makeText(this@UploadGalleryActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
                                return@launch
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
                    galleryLauncher.launch(intent)
                }
            }
        }

}