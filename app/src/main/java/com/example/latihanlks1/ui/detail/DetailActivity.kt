package com.example.latihanlks1.ui.detail

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.example.latihanlks1.data.model.Album
import com.example.latihanlks1.databinding.ActivityDetailBinding
import com.example.latihanlks1.ui.galery.GalleryActivity
import com.example.latihanlks1.util.loadBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DetailActivity : AppCompatActivity() {
    lateinit var binding : ActivityDetailBinding
    companion object{
        const val EXTRA_ALBUM = "Extra_Album"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)

        }

        val album = intent.getParcelableExtra<Album>(EXTRA_ALBUM) as Album
        val id = album.id
        val name = album.name
        val desc = album.descrption
        binding.tvDetailid.text = id
        binding.tvDetailname.text = name
        binding.tvDetaildescription.text = desc
        bind(album)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun bind(detailcat : Album){
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO){
                detailcat.imageUrl?.let {
                    loadBitmap(it)
                }
            }
            bitmap?.let {
                binding.ivDetailphoto.setImageBitmap(it)
            }
        }
    }
}