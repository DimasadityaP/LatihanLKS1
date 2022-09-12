package com.example.latihanlks1.ui.Galery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.latihanlks1.Data.Model.Photo
import com.example.latihanlks1.Util.loadbitmap
import com.example.latihanlks1.databinding.ImagePhotoBinding
import kotlinx.coroutines.*

class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.Photoviewholder>(){
    class Photoviewholder(val binding :ImagePhotoBinding) : RecyclerView.ViewHolder(binding.root)

    private val list: MutableList<Photo> = mutableListOf()

    fun addImages(photo : List<Photo>){
        list.clear()
        list.addAll(photo)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Photoviewholder {
        val inflater = LayoutInflater.from(parent.context)
        return Photoviewholder(ImagePhotoBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: Photoviewholder, position: Int) {

    }

    private fun bind(binding: ImagePhotoBinding, photo : Photo){
        binding.tvPhoto.text = photo.name

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO){
                photo.image?.let {
                    loadbitmap(it)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}