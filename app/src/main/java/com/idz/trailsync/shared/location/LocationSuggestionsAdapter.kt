package com.idz.trailsync.shared.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.idz.trailsync.databinding.LocationSuggestionItemBinding

class LocationSuggestionsAdapter(
    private val onSuggestionClick: (AutocompletePrediction) -> Unit
) : RecyclerView.Adapter<LocationSuggestionRowViewHolder>() {

    private var suggestions = listOf<AutocompletePrediction>()

    fun setSuggestions(newSuggestions: List<AutocompletePrediction>) {
        suggestions = newSuggestions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationSuggestionRowViewHolder {
        val binding = LocationSuggestionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationSuggestionRowViewHolder(binding, onSuggestionClick)
    }

    override fun onBindViewHolder(holder: LocationSuggestionRowViewHolder, position: Int) {
        holder.bind(suggestions[position])
    }

    override fun getItemCount(): Int = suggestions.size
}
