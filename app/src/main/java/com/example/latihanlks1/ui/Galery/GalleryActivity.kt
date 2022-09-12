package com.example.latihanlks1.ui.Galery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.latihanlks1.Data.Model.Photo
import com.example.latihanlks1.Data.Network.NetworkApi
import com.example.latihanlks1.R
import com.example.latihanlks1.databinding.ActivityGalleryBinding
import com.example.latihanlks1.databinding.ImagePhotoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class GalleryActivity : AppCompatActivity() {
    lateinit var binding : ActivityGalleryBinding
    lateinit var galleryadapter : GalleryAdapter
    private var list = listOf<Photo>()
    private var searchlist = listOf<Photo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setUp()
    }

    private fun setUp(){
        galleryadapter = GalleryAdapter()
        binding.RvImage.adapter = galleryadapter
        mockApi()
    }
    private suspend fun getCats(): JSONArray? {
        return withContext(Dispatchers.IO){
            NetworkApi("https://api.thecatapi.com/v1/breeds?limit=20&page=0")
                .execute()?.let {
                    JSONArray(it)
                }
        }
    }
    private fun mockApi(){
        CoroutineScope(Dispatchers.Main).launch {
            val jsonArray = getCats()
            jsonArray?.let {
                val templist = mutableListOf<Photo>()

                for (i in 0 until it.length()){
                    val item = it.getJSONObject(i)
                    val image = item.getJSONObject("image")
                    templist.add(
                        Photo(
                            album = null,
                            name = item.getString("id"),
                            size = 20000,
                            id = UUID.randomUUID().toString(),
                            albumId = null,
                            userId = UUID.randomUUID().toString(),
                            createdAt = "2021-10-25T22:45:52.000000Z",
                            updatedAt = "2021-10-25T16:45:52.000000Z",
                            image = image.getString("url"),
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