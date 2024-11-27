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
    private val onSpiderClick: (Spider) -> Unit,
    private val onFavoriteClick: (Spider) -> Unit
) : RecyclerView.Adapter<SpiderAdapter.SpiderViewHolder>() {

    // Add companion object with shared color logic
    companion object {
        fun getStatusColor(status: String, context: android.content.Context): Int {
            return when (status) {
                "Healthy" -> context.getColor(R.color.status_healthy)
                "Injured" -> context.getColor(R.color.status_injured)
                "Unavailable" -> context.getColor(R.color.status_unavailable)
                else -> context.getColor(R.color.gray)
            }
        }
    }

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

        // Set status color based on health status
        val statusColor = when (spider.spiderHealthStatus) {
            "Healthy" -> holder.itemView.context.getColor(R.color.status_healthy)
            "Injured" -> holder.itemView.context.getColor(R.color.status_injured)
            "Unavailable" -> holder.itemView.context.getColor(R.color.status_unavailable)
            else -> holder.itemView.context.getColor(R.color.gray)
        }
        holder.spiderStatus.setTextColor(statusColor)

        // Load spider image using Glide
        Glide.with(holder.itemView.context)
            .load(spider.spiderImageRef)
            .placeholder(R.drawable.spider_sample_img)
            .error(R.drawable.spider_sample_img)
            .into(holder.spiderImage)

        // Update star button image based on favorite status
        holder.starButton.setImageResource(
            if (spider.spiderIsFavorite == 1) {
                R.drawable.ic_star_on}
            else {R.drawable.ic_star_off}
        )

        // Set click listener for star button
        holder.starButton.setOnClickListener {
            onFavoriteClick(spider)
        }

        // Set click listener for the whole item
        holder.itemView.setOnClickListener {
            onSpiderClick(spider)
        }
    }

    override fun getItemCount() = spiders.size

    fun updateSpiders(newSpiders: List<Spider>) {
        spiders = newSpiders
        notifyDataSetChanged()
    }
}