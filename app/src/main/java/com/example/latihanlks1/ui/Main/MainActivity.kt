package com.example.latihanlks1.ui.Main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.latihanlks1.databinding.ActivityMainBinding
import com.example.latihanlks1.ui.galery.GalleryActivity
import com.example.latihanlks1.ui.galery.UploadGalleryActivity

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setUp()
    }

    private fun setUp() {
        binding.btnFormRequest.setOnClickListener {
            val intentupload = Intent(this, UploadGalleryActivity::class.java)
            startActivity(intentupload)
        }

        binding.btnList.setOnClickListener {
            val intentList = Intent(this, GalleryActivity::class.java)
            startActivity(intentList)
        }


    }
}