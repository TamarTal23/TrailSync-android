package com.idz.trailsync.features.editPost

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.libraries.places.api.model.Place
import com.idz.trailsync.R
import com.idz.trailsync.databinding.FragmentEditPostBinding
import com.idz.trailsync.features.createPost.location.LocationAutocompleteController
import com.idz.trailsync.features.post.photo.PhotoAdapter
import com.idz.trailsync.model.Post
import java.util.Date

class EditPostFragment : Fragment() {

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditPostViewModel by viewModels()
    private val args: EditPostFragmentArgs by navArgs()

    private var currentDuration = 1
    private val selectedPhotos = mutableListOf<Any>()
    private lateinit var photosAdapter: PhotoAdapter<Any>
    private var selectedPlace: Place? = null
    private var locationController: LocationAutocompleteController? = null

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
            if (uris.isNotEmpty()) {
                val totalAllowed = 10
                val remainingSlots = totalAllowed - selectedPhotos.size
                val urisToAdd = uris.take(remainingSlots)

                if (uris.size > remainingSlots) {
                    context?.let {
                        Toast.makeText(
                            it,
                            "Maximum $totalAllowed photos allowed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                selectedPhotos.addAll(urisToAdd)
                updatePhotosUI()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postToEdit = args.post
        viewModel.setPost(postToEdit)

        setupUI()
        setupPhotosRecyclerView()
        setupLocationController()
        populateData(postToEdit)
        observeViewModel()
    }

    private fun setupUI() {
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
                context?.let {
                    Toast.makeText(it, "Maximum 10 photos reached", Toast.LENGTH_SHORT).show()
                }
            } else {
                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }

        binding.updatePostButton.setOnClickListener {
            if (validateInput()) {
                updatePost()
            }
        }
    }

    private fun setupLocationController() {
        context?.let { ctx ->
            locationController = LocationAutocompleteController(
                ctx,
                binding.locationSearchEditText,
                binding.locationSuggestionsRecyclerView
            ) { place ->
                selectedPlace = place
            }
        }
    }

    private fun setupPhotosRecyclerView() {
        photosAdapter = PhotoAdapter(
            photos = selectedPhotos.toList(),
            onRemoveClick = { photo ->
                selectedPhotos.remove(photo)
                updatePhotosUI()
            }
        )
        binding.selectedPhotosRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = photosAdapter
        }
    }

    private fun populateData(post: Post) {
        binding.tripTitleEditText.setText(post.title)
        binding.googleMapsEditText.setText(post.mapLink)
        binding.locationSearchEditText.setText(post.location?.name ?: "")
        binding.durationEditText.setText(post.numberOfDays.toString())
        currentDuration = post.numberOfDays
        binding.priceEditText.setText(post.price.toString())
        binding.descriptionEditText.setText(post.description)

        selectedPhotos.clear()
        selectedPhotos.addAll(post.photos)
        updatePhotosUI()
    }

    private fun updatePhotosUI() {
        photosAdapter.photos = selectedPhotos.toList()
        photosAdapter.notifyDataSetChanged()
        binding.selectedPhotosRecyclerView.visibility =
            if (selectedPhotos.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateDurationUI() {
        if (binding.durationEditText.text.toString() != currentDuration.toString()) {
            binding.durationEditText.setText(currentDuration.toString())
            binding.durationEditText.setSelection(binding.durationEditText.text.length)
        }
    }

    private fun observeViewModel() {
        viewModel.isUpdating.observe(viewLifecycleOwner) { isUpdating ->
            binding.updatePostButton.isEnabled = !isUpdating
        }
    }

    private fun updatePost() {
        val originalPost = viewModel.post.value ?: return

        val title = binding.tripTitleEditText.text.toString().trim()
        val mapLink = binding.googleMapsEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val price = binding.priceEditText.text.toString().toIntOrNull() ?: 0

        val location = selectedPlace?.let { place ->
            var city = ""
            var country = ""
            place.addressComponents?.asList()?.forEach { component ->
                when {
                    component.types.contains("locality") -> city = component.name ?: ""
                    component.types.contains("country") -> country = component.name ?: ""
                }
            }
            Post.Location(
                city = city,
                country = country,
                name = place.name ?: "",
                lat = place.location?.latitude ?: 0.0,
                lng = place.location?.longitude ?: 0.0,
                placeId = place.id ?: ""
            )
        } ?: originalPost.location

        val existingUrls = selectedPhotos.filterIsInstance<String>()
        val newLocalUris = selectedPhotos.filterIsInstance<Uri>()
        val newBitmaps = newLocalUris.mapNotNull { uriToBitmap(it) }

        val updatedPost = originalPost.copy(
            title = title,
            description = description,
            location = location,
            numberOfDays = currentDuration,
            price = price,
            mapLink = mapLink,
            photos = existingUrls,
            updatedAt = Date()
        )

        viewModel.updatePost(updatedPost, newBitmaps) { success ->
            if (isAdded && activity != null) {
                val ctx = context
                if (ctx != null) {
                    if (success) {
                        Toast.makeText(ctx, "Post updated successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(ctx, "Failed to update post", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val ctx = context ?: return null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(ctx.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(ctx.contentResolver, uri)
            }
        } catch (e: Exception) {
            Log.d("EditPostFragment", "Error converting URI to bitmap: ${e.message}")
            null
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
        }
        return isValid
    }

    private fun resetErrors() {
        context?.let { ctx ->
            val normalBg = ContextCompat.getDrawable(ctx, R.drawable.edit_text_background)
            val normalTextColor = ContextCompat.getColor(ctx, R.color.black)
            binding.tripTitleLabel.setTextColor(normalTextColor)
            binding.tripTitleEditText.background = normalBg
            binding.tripTitleError.visibility = View.GONE
            binding.googleMapsLabel.setTextColor(normalTextColor)
            binding.googleMapsContainer.background = normalBg
            binding.googleMapsError.visibility = View.GONE
        }
    }

    private fun showTripTitleError(error: String) {
        context?.let { ctx ->
            val errorBg =
                ContextCompat.getDrawable(ctx, R.drawable.edit_text_error_background)
            val errorColor = ContextCompat.getColor(ctx, R.color.error_red)
            binding.tripTitleLabel.setTextColor(errorColor)
            binding.tripTitleEditText.background = errorBg
            binding.tripTitleError.text = error
            binding.tripTitleError.visibility = View.VISIBLE
        }
    }

    private fun showGoogleMapsError(error: String) {
        context?.let { ctx ->
            val errorBg =
                ContextCompat.getDrawable(ctx, R.drawable.edit_text_error_background)
            val errorColor = ContextCompat.getColor(ctx, R.color.error_red)
            binding.googleMapsLabel.setTextColor(errorColor)
            binding.googleMapsContainer.background = errorBg
            binding.googleMapsError.text = error
            binding.googleMapsError.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
