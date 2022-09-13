package com.example.latihanlks1.ui.galery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.latihanlks1.data.model.Photo
import com.example.latihanlks1.databinding.ImagePhotoBinding
import com.example.latihanlks1.util.loadBitmap
import kotlinx.coroutines.*

class GalleryAdapter(private val onClick: (Photo)-> Unit) : RecyclerView.Adapter<GalleryAdapter.Photoviewholder>(){
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
        bind(holder.binding, list[position])
    }

    private fun bind(binding: ImagePhotoBinding, photo : Photo){
        binding.tvPhoto.text = photo.name
        //Cara 1
//        binding.root.setOnClickListener{
//            onClick(photo)
//        }

        //Cara2
        binding.root.setOnClickListener{
            onClick.invoke(photo)
        }

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO){
                photo.image?.let {
                    loadBitmap(it)
                }
            }
            bitmap?.let {
                binding.ivPhoto.setImageBitmap(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}