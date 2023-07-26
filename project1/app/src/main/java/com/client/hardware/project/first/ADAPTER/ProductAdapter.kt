package com.client.hardware.project.first.ADAPTER

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.client.hardware.project.first.MODEL.Product
import com.client.hardware.project.first.R
import java.util.Locale


class ProductAdapter(private val productlist: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>(), Filterable {
    private var filteredList: List<Product> = productlist.toList()
    private var selectedQuality = ""
    private var selectedDimension = ""
    private var searchText: String = ""

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductId: TextView = itemView.findViewById(R.id.tvProductId)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductQuality: TextView = itemView.findViewById(R.id.tvProductQuality)
        private val tvProductDimensions: TextView =
            itemView.findViewById(R.id.tvProductDimensions)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)

        fun bind(product: Product) {
            tvProductId.text = "ID: ${product.productId}"
            tvProductName.text = "Product Name: ${product.productName}"
            tvProductQuality.text = "Quality: ${product.quality}"
            tvProductDimensions.text = "Dimensions: ${product.dimensions}"
            tvProductPrice.text = "Price: ${product.pricePerUnit}/-"

            Glide.with(itemView)
                .load(product.imageUrl)
                .placeholder(R.drawable.icn_default_iv)
                .into(ivProductImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = filteredList[position]
        holder.bind(product)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                searchText = constraint.toString().trim()
                filteredList = if (searchText.isNotEmpty()) {
                    productlist.filter { product ->
                        product.productName?.contains(searchText, true) ?: true
                    }
                } else {
                    applyFilter()
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                @Suppress("UNCHECKED_CAST")
                filteredList = results.values as List<Product>
                notifyDataSetChanged()
            }
        }
    }


    fun applyFilter(selectedQuality: String, selectedDimension: String) {
        this.selectedQuality = selectedQuality
        this.selectedDimension = selectedDimension
        filter.filter(searchText) // Trigger filtering process
    }

    fun clearFilters() {
        selectedQuality = ""
        selectedDimension = ""
        filter.filter("") // Trigger filtering process
    }

    private fun applyFilter(): List<Product> {
        return if (searchText.isEmpty() && selectedQuality.isEmpty() && selectedDimension.isEmpty()) {
            productlist.toList()
        } else {
            productlist.filter { product ->
                val nameMatch = product.productName?.contains(searchText, true) ?: true
                val qualityMatch = product.quality?.equals(selectedQuality, true) ?: true
                val dimensionMatch = product.dimensions?.equals(selectedDimension, true) ?: true
                nameMatch && qualityMatch && dimensionMatch
            }
        }
    }
}