package com.capstone.gagambrawl.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.gagambrawl.databinding.ListItemHomeCatalogBinding
import com.capstone.gagambrawl.model.Catalog
import android.util.Log

class CatalogHomeAdapter : RecyclerView.Adapter<CatalogHomeAdapter.CatalogViewHolder>() {
    companion object {
        private var catalogs = listOf<Catalog>()
    }

    inner class CatalogViewHolder(private val binding: ListItemHomeCatalogBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(catalog: Catalog) {
            binding.apply {
                spiderBreed.text = catalog.catalogName
                spiderDescription.text = catalog.catalogDescription
                Glide.with(itemView.context)
                    .load(catalog.catalogImageRef)
                    .into(spiderImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val binding = ListItemHomeCatalogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CatalogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        holder.bind(catalogs[position])
    }

    override fun getItemCount() = catalogs.size

    fun updateCatalogs(newCatalogs: List<Catalog>) {
        if (catalogs.isEmpty()) {
            Log.d("CatalogHomeAdapter", "Updating catalogs: ${newCatalogs.size}")
            catalogs = newCatalogs
            notifyDataSetChanged()
        }
    }

    fun isEmpty(): Boolean = catalogs.isEmpty()

    fun clearCache() {
        catalogs = listOf()
        notifyDataSetChanged()
    }
} 