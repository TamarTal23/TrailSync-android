package com.idz.trailsync

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.idz.trailsync.databinding.FragmentCreatePostBinding
import com.idz.trailsync.features.home.post.photo.PhotoAdapter

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private var currentDuration = 1
    private val selectedPhotos = mutableListOf<Uri>()
    private lateinit var photosAdapter: PhotoAdapter<Uri>

    private val pickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        if (uris.isNotEmpty()) {
            val remainingSlots = 10 - selectedPhotos.size
            val urisToAdd = uris.take(remainingSlots)
            
            if (uris.size > remainingSlots) {
                Toast.makeText(context, "You can only select up to 10 photos", Toast.LENGTH_SHORT).show()
            }
            
            selectedPhotos.addAll(urisToAdd)
            photosAdapter.photos = selectedPhotos.toList()
            photosAdapter.notifyDataSetChanged()
            updatePhotosVisibility()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        setupUI()
        setupPhotosRecyclerView()
        return binding.root
    }

    private fun setupUI() {
        // Initial values
        binding.durationEditText.setText(currentDuration.toString())
        binding.priceEditText.hint = "$ 0"

        // Stepper logic
        binding.btnIncrement.setOnClickListener {
            currentDuration++
            updateDurationUI()
        }

        binding.btnDecrement.setOnClickListener {
            if (currentDuration > 1) {
                currentDuration--
                updateDurationUI()
            }
        }

        binding.durationEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().toIntOrNull()
                if (input != null && input != currentDuration) {
                    currentDuration = input
                }
            }
        })

        binding.addPhotosButton.setOnClickListener {
            if (selectedPhotos.size >= 10) {
                Toast.makeText(context, "Maximum 10 photos reached", Toast.LENGTH_SHORT).show()
            } else {
                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }

        binding.createPostButton.setOnClickListener {
            if (validateInput()) {
                // Logic to save the post will go here later
                Toast.makeText(context, "Post created successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupPhotosRecyclerView() {
        photosAdapter = PhotoAdapter(
            photos = selectedPhotos.toList(),
            onRemoveClick = { uri ->
                selectedPhotos.remove(uri)
                photosAdapter.photos = selectedPhotos.toList()
                photosAdapter.notifyDataSetChanged()
                updatePhotosVisibility()
            }
        )
        binding.selectedPhotosRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = photosAdapter
        }
        updatePhotosVisibility()
    }

    private fun updatePhotosVisibility() {
        binding.selectedPhotosRecyclerView.visibility = if (selectedPhotos.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateDurationUI() {
        if (binding.durationEditText.text.toString() != currentDuration.toString()) {
            binding.durationEditText.setText(currentDuration.toString())
            binding.durationEditText.setSelection(binding.durationEditText.text.length)
        }
    }

    private fun validateInput(): Boolean {
        val title = binding.tripTitleEditText.text.toString().trim()
        val mapLink = binding.googleMapsEditText.text.toString().trim()
        var isValid = true

        resetErrors()

        if (title.isEmpty()) {
            showTripTitleError("Title is required")
            isValid = false
        }

        if (mapLink.isEmpty()) {
            showGoogleMapsError("Google Maps URL is required")
            isValid = false
        } else if (!isGoogleMapsUrl(mapLink)) {
            showGoogleMapsError("Please provide a valid Google Maps link")
            isValid = false
        }

        return isValid
    }

    private fun resetErrors() {
        val normalBg = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_background)
        val normalTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        binding.tripTitleLabel.setTextColor(normalTextColor)
        binding.tripTitleEditText.background = normalBg
        binding.tripTitleError.visibility = View.GONE

        binding.googleMapsLabel.setTextColor(normalTextColor)
        binding.googleMapsContainer.background = normalBg
        binding.googleMapsError.visibility = View.GONE
    }

    private fun showTripTitleError(error: String) {
        val errorBg = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_error_background)
        val errorColor = ContextCompat.getColor(requireContext(), R.color.error_red)

        binding.tripTitleLabel.setTextColor(errorColor)
        binding.tripTitleEditText.background = errorBg
        binding.tripTitleError.text = error
        binding.tripTitleError.visibility = View.VISIBLE
    }

    private fun showGoogleMapsError(error: String) {
        val errorBg = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_error_background)
        val errorColor = ContextCompat.getColor(requireContext(), R.color.error_red)

        binding.googleMapsLabel.setTextColor(errorColor)
        binding.googleMapsContainer.background = errorBg
        binding.googleMapsError.text = error
        binding.googleMapsError.visibility = View.VISIBLE
    }

    private fun isGoogleMapsUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.contains("google.com/maps") || 
               lowerUrl.contains("maps.google.com") || 
               lowerUrl.contains("goo.gl/maps") ||
               lowerUrl.contains("maps.app.goo.gl") ||
               lowerUrl.contains("google.co.il/maps")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
