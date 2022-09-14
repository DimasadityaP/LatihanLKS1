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
            val intentupload = Intent(this, UploadGalleryActivity::class.java)
            startActivity(intentupload)
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
