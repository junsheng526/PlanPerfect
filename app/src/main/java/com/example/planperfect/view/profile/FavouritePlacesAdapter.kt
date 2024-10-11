package com.example.planperfect.view.profile

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.FailedModalBinding
import com.example.planperfect.databinding.ItemFavouritePlaceBinding
import com.example.planperfect.databinding.SuccessModalBinding
import com.example.planperfect.databinding.WarningModalBinding
import com.example.planperfect.utils.FavoritesManager
import com.example.planperfect.view.planning.PlacesDetailsActivity
import kotlinx.coroutines.launch

class FavouritePlacesAdapter(
    private val favoritesManager: FavoritesManager
) : RecyclerView.Adapter<FavouritePlacesAdapter.PlaceViewHolder>() {

    private val placesList = mutableListOf<TouristPlace>()

    fun submitList(places: List<TouristPlace>) {
        placesList.clear()
        placesList.addAll(places)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemFavouritePlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(placesList[position])
    }

    override fun getItemCount(): Int = placesList.size

    inner class PlaceViewHolder(private val binding: ItemFavouritePlaceBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(touristPlace: TouristPlace) {
            binding.placeName.text = touristPlace.name
            binding.placeCategory.text = touristPlace.category
            binding.itemDescription.text = touristPlace.description

            val imageUrl = touristPlace.imageUrls.firstOrNull()
            Glide.with(binding.itemImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.tourist_image_1)
                .into(binding.itemImage)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, PlacesDetailsActivity::class.java).apply {
                    putExtra("place", touristPlace)
                }
                context.startActivity(intent)
            }

            binding.closeButton.setOnClickListener {
                // Show confirmation dialog for removal
                showConfirmationDialog(
                    binding.root.context,
                    "Remove Favorite",
                    "Are you sure you want to remove ${touristPlace.name} from your favorites?"
                ) {
                    removeFavorite(touristPlace)
                }
            }
        }

        private fun removeFavorite(touristPlace: TouristPlace) {
            // Cast context to AppCompatActivity to access lifecycleScope
            val context = binding.root.context as? AppCompatActivity
            context?.lifecycleScope?.launch {
                try {
                    // Call the method to remove the favorite place
                    favoritesManager.removeFavorite(touristPlace.id)

                    // Remove the place from the list and notify the adapter
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        placesList.remove(touristPlace)
                        notifyItemRemoved(position)
                        showSuccessDialog(context, "Success", "${touristPlace.name} removed from favorites.")
                    }
                } catch (e: Exception) {
                    showErrorDialog(context, "Error", "Failed to remove ${touristPlace.name} from favorites.")
                }
            }
        }
    }

    private fun showConfirmationDialog(context: Context, title: String, description: String, onConfirm: () -> Unit) {
        val dialogViewBinding = WarningModalBinding.inflate(LayoutInflater.from(context))

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogViewBinding.root)
            .create()

        dialogViewBinding.modalTitle.text = title
        dialogViewBinding.modalDesc.text = description

        dialogViewBinding.btnConfirm.setOnClickListener {
            onConfirm()
            dialogBuilder.dismiss()
        }

        dialogViewBinding.btnBack.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.show()
    }

    private fun showSuccessDialog(context: Context, title: String, description: String) {
        val dialogViewBinding = SuccessModalBinding.inflate(LayoutInflater.from(context))

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogViewBinding.root)
            .create()

        dialogViewBinding.modalTitle.text = title
        dialogViewBinding.modalDesc.text = description

        dialogViewBinding.btnBack.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.show()
    }

    private fun showErrorDialog(context: Context, title: String, description: String) {
        val dialogViewBinding = FailedModalBinding.inflate(LayoutInflater.from(context))

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogViewBinding.root)
            .create()

        dialogViewBinding.modalTitle.text = title
        dialogViewBinding.modalDesc.text = description

        dialogViewBinding.btnBack.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.show()
    }
}
