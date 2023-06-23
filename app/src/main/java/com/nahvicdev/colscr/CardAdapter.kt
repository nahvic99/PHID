package com.nahvicdev.colscr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CardAdapter(private val photos: List<Photo>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val photo = photos[position]
        Picasso.get().load(photo.src.medium).into(holder.imageView)
    }


    override fun getItemCount(): Int {
        return photos.size
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
    }

    data class Photo(
        val id: Int,
        val src: Src
    )

    data class Src(
        val original: String,
        val large2x: String,
        val large: String,
        val medium: String,
        val small: String,
        val portrait: String,
        val landscape: String,
        val tiny: String
    )
}