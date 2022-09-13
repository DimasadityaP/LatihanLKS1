package com.example.latihanlks1.ui.galery

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.latihanlks1.data.model.Album
import com.example.latihanlks1.data.model.Photo
import com.example.latihanlks1.data.network.NetworkApi
import com.example.latihanlks1.databinding.ActivityGalleryBinding
import com.example.latihanlks1.ui.detail.DetailActivity
import com.example.latihanlks1.util.getRealPathFromURI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GalleryActivity : AppCompatActivity() {
    lateinit var binding: ActivityGalleryBinding
    lateinit var galleryadapter: GalleryAdapter
    private var list = listOf<Photo>()
    private var searchlist = listOf<Photo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setUp()
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Log.d("ActivityResultGallery", it.data?.data.toString())
                it.data?.data?.let { uri ->
                    getRealPathFromURI(uri)?.let { realPath ->
                        Log.d("getRealPathFromURI", realPath)
                        val file = File(realPath)
                        CoroutineScope(Dispatchers.Main).launch {
                            postImage(file)?.let {
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

    private fun setUp() {
        galleryadapter = GalleryAdapter { photo ->
            val intent = Intent(this, DetailActivity::class.java)
            CoroutineScope(Dispatchers.Main).launch {
                val detailObject = getDetailCats(photo.id)
                detailObject?.let {
                    val idCat = it.getString("id")
                    val nameCat = it.getString("name")
                    val desc = it.getString("description")
                    val imgurl = photo.image.toString()
                    val album =
                        Album(id = idCat, name = nameCat, descrption = desc, imageUrl = imgurl)
                    intent.putExtra(DetailActivity.EXTRA_ALBUM, album)
                    startActivity(intent)
                }
            }
        }
        binding.RvImage.adapter = galleryadapter
        connectionCatApi()

        binding.btnAdd.setOnClickListener {
            storagePermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private suspend fun getCats(): JSONArray? {
        return withContext(Dispatchers.IO) {
            NetworkApi("https://api.thecatapi.com/v1/breeds?limit=20&page=0")
                .execute()?.let {
                    JSONArray(it)
                }
        }
    }

    private suspend fun postImage(file: File): JSONObject? {
        return try {
            withContext(Dispatchers.IO) {
                NetworkApi(
                    "https://api.thecatapi.com/v1/images/upload",
                    method = "POST",
                    headers = arrayOf(
                        Pair("x-api-key", "live_F9QVdZ5GmaliK9O0lugJmvTckMYhL74IrOll5JGyPA2UchBKC5CvPCD7s0lUBb7d"),
                    ),
                ).addFilePart("file", file)
                    .execute()?.let {
                        JSONObject(it)
                    }
            }
        } catch (e: Exception) {
            Toast.makeText(this@GalleryActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            null
        }
    }

    private suspend fun getDetailCats(id: String?): JSONObject? {
        return withContext(Dispatchers.IO) {
            NetworkApi("https://api.thecatapi.com/v1/breeds/$id")
                .execute()?.let {
                    JSONObject(it)
                }
        }
    }

    private fun connectionCatApi() {
        CoroutineScope(Dispatchers.Main).launch {
            val jsonArray = getCats()
            jsonArray?.let {
                val templist = mutableListOf<Photo>()

                for (i in 0 until it.length()) {
                    val item = it.getJSONObject(i)
                    val image = item.getJSONObject("image")
                    templist.add(
                        Photo(
                            name = item.getString("name"),
                            id = item.getString("id"),
                            image = image.getString("url")
                        )
                    )
                }
                list = templist
                searchlist = list
                galleryadapter.addImages(searchlist)
            }
        }
    }
}
