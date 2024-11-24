package com.capstone.gagambrawl.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.databinding.ListItemInventoryHomeBinding
import com.capstone.gagambrawl.model.Spider

class HomeInventoryAdapter(
    private val onSpiderClick: (Spider) -> Unit
) : RecyclerView.Adapter<HomeInventoryAdapter.ViewHolder>() {

    private var spiders: List<Spider> = emptyList()

    class ViewHolder(val binding: ListItemInventoryHomeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemInventoryHomeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spider = spiders[position]
        holder.binding.apply {
            inventorySpiderName.text = spider.spiderName
            inventorySpiderStatus.text = spider.spiderHealthStatus

            Glide.with(inventoryImage.context)
                .load(spider.spiderImageRef)
                .placeholder(R.drawable.spider_sample_img)
                .error(R.drawable.spider_sample_img)
                .into(inventoryImage)

            root.setOnClickListener { onSpiderClick(spider) }
        }
    }

    override fun getItemCount() = spiders.size

    fun updateSpiders(newSpiders: List<Spider>) {
        spiders = newSpiders
        notifyDataSetChanged()
    }
}