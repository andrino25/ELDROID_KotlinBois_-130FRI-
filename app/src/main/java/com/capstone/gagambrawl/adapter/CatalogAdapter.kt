package com.capstone.gagambrawl.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.model.Catalog

class CatalogAdapter : RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {
    private var catalogs = listOf<Catalog>()

    class CatalogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val spiderImage: ImageView = view.findViewById(R.id.spider_image)
        val spiderBreed: TextView = view.findViewById(R.id.spider_breed)
        val spiderDescription: TextView = view.findViewById(R.id.spider_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_catalog, parent, false)
        return CatalogViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        val catalog = catalogs[position]
        
        holder.spiderBreed.text = catalog.catalogName
        holder.spiderDescription.text = catalog.catalogDescription
        
        Glide.with(holder.spiderImage.context)
            .load(catalog.catalogImageRef)
            .placeholder(R.drawable.spider_sample_img)
            .error(R.drawable.spider_sample_img)
            .into(holder.spiderImage)
    }

    override fun getItemCount() = catalogs.size

    fun updateCatalogs(newCatalogs: List<Catalog>) {
        catalogs = newCatalogs
        notifyDataSetChanged()
    }
} 