package com.idz.trailsync.features.createPost.location

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class LocationAutocompleteController(
    private val context: Context,
    private val searchEditText: EditText,
    private val suggestionsRecyclerView: RecyclerView,
    private val onPlaceSelected: (Place) -> Unit
) {
    private val placesClient: PlacesClient = Places.createClient(context)
    private val suggestionsAdapter: LocationSuggestionsAdapter
    private var isSelectingFromSuggestions = false

    init {
        suggestionsAdapter = LocationSuggestionsAdapter { prediction ->
            selectSuggestion(prediction)
        }

        suggestionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = suggestionsAdapter
        }

        setupSearchEditText()
    }

    private fun setupSearchEditText() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(sequence: Editable?) {
                if (isSelectingFromSuggestions) {
                    isSelectingFromSuggestions = false
                    return
                }
                val query = sequence?.toString() ?: ""

                if (query.length >= 3) {
                    fetchAutocompletePredictions(query)
                } else {
                    suggestionsRecyclerView.visibility = View.GONE
                }
            }
        })
    }

    private fun fetchAutocompletePredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                Log.d("MAPS_API", "Found ${predictions.size} predictions")
                if (predictions.isNotEmpty()) {
                    suggestionsAdapter.setSuggestions(predictions)
                    suggestionsRecyclerView.visibility = View.VISIBLE
                } else {
                    suggestionsRecyclerView.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MAPS_API", "Autocomplete failed: ${exception.message}")
                suggestionsRecyclerView.visibility = View.GONE
            }
    }

    private fun selectSuggestion(prediction: AutocompletePrediction) {
        isSelectingFromSuggestions = true
        searchEditText.setText(prediction.getPrimaryText(null))
        suggestionsRecyclerView.visibility = View.GONE

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LOCATION,
            Place.Field.ADDRESS_COMPONENTS
        )
        val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                Log.d("MAPS_API", "Place fetched: ${response.place.name}")
                onPlaceSelected(response.place)
            }
            .addOnFailureListener { exception ->
                Log.e("MAPS_API", "Fetch place failed: ${exception.message}")
                Toast.makeText(context, "Failed to get location details", Toast.LENGTH_SHORT).show()
            }
    }
}
