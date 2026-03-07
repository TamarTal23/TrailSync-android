package com.idz.trailsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.idz.trailsync.databinding.FragmentCreatePostBinding

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        // Duration and Price are fixed as per requirement
        binding.durationValue.text = "1"
        binding.priceValue.text = "$ 0"

        binding.createPostButton.setOnClickListener {
            if (validateInput()) {
                // Logic to save the post will go here later
                Toast.makeText(context, "Post created successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun validateInput(): Boolean {
        val title = binding.tripTitleEditText.text.toString().trim()
        val mapLink = binding.googleMapsEditText.text.toString().trim()
        var isValid = true

        // Reset errors
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
