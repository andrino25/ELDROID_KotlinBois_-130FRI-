package com.capstone.gagambrawl.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.model.Spider
import com.google.android.material.imageview.ShapeableImageView

class SpiderAdapter(
    private var spiders: List<Spider>,
    private val onSpiderClick: (Spider) -> Unit
) : RecyclerView.Adapter<SpiderAdapter.SpiderViewHolder>() {

    class SpiderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val spiderImage: ShapeableImageView = view.findViewById(R.id.i_spider_image)
        val spiderBreed: TextView = view.findViewById(R.id.i_spider_breed)
        val spiderStatus: TextView = view.findViewById(R.id.i_spider_status)
        val starButton: ImageButton = view.findViewById(R.id.starBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpiderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_inventory, parent, false)
        return SpiderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpiderViewHolder, position: Int) {
        val spider = spiders[position]
        
        holder.spiderBreed.text = spider.spiderName
        holder.spiderStatus.text = spider.spiderHealthStatus

        // Load spider image using Glide
        Glide.with(holder.itemView.context)
            .load(spider.spiderImageRef)
            .placeholder(R.drawable.spider_sample_img)
            .error(R.drawable.spider_sample_img)
            .into(holder.spiderImage)

        // Set click listener for the whole item
        holder.itemView.setOnClickListener {
            onSpiderClick(spider)
        }

        // Optional: Set click listener for star button
        holder.starButton.setOnClickListener {
            // TODO: Implement favorite functionality
        }
    }

    override fun getItemCount() = spiders.size

    fun updateSpiders(newSpiders: List<Spider>) {
        spiders = newSpiders
        notifyDataSetChanged()
    }
}