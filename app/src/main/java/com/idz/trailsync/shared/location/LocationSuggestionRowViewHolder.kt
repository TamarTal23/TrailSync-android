package com.idz.trailsync.shared.location

import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.idz.trailsync.databinding.LocationSuggestionItemBinding

class LocationSuggestionRowViewHolder(
    private val binding: LocationSuggestionItemBinding,
    private val onSuggestionClick: (AutocompletePrediction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var prediction: AutocompletePrediction? = null

    init {
        itemView.setOnClickListener {
            prediction?.let {
                onSuggestionClick(it)
            }
        }
    }

    fun bind(prediction: AutocompletePrediction) {
        this.prediction = prediction
        binding.suggestionText.text = prediction.getFullText(null)
    }
}
