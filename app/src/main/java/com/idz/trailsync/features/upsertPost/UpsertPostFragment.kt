package com.idz.trailsync.features.upsertPost

import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.R
import com.idz.trailsync.databinding.FragmentUpsertPostBinding
import com.idz.trailsync.features.createPost.location.LocationAutocompleteController
import com.idz.trailsync.features.post.photo.PhotoAdapter
import com.idz.trailsync.model.Post
import java.util.Date
import java.util.UUID

class UpsertPostFragment : Fragment() {

    private var _binding: FragmentUpsertPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UpsertPostViewModel by viewModels()
    private val args: UpsertPostFragmentArgs by navArgs()

    private var currentDuration = 1
    private val selectedPhotos = mutableListOf<Any>()
    private lateinit var photosAdapter: PhotoAdapter<Any>
    private var selectedPlace: Place? = null
    private var locationController: LocationAutocompleteController? = null

    private val pickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        if (uris.isNotEmpty()) {
            val totalAllowed = 10
            val remainingSlots = totalAllowed - selectedPhotos.size
            val urisToAdd = uris.take(remainingSlots)

            if (uris.size > remainingSlots) {
                context?.let {
                    Toast.makeText(it, "Maximum $totalAllowed photos allowed", Toast.LENGTH_SHORT).show()
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
        _binding = FragmentUpsertPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val post = args.post
        viewModel.setPost(post)

        setupUI()
        setupPhotosRecyclerView()
        setupLocationController()
        observeViewModel()

        if (post != null) {
            populateData(post)
        } else {
            binding.headerTitle.text = "Share Your Trail"
            binding.upsertPostButton.text = "Create Post"
        }
    }

    private fun setupUI() {
        binding.durationEditText.setText(currentDuration.toString())
        binding.priceEditText.hint = "$ 0"

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

        binding.upsertPostButton.setOnClickListener {
            if (validateInput()) {
                handleUpsert()
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
        binding.headerTitle.text = "Edit Your Trail"
        binding.upsertPostButton.text = "Update Post"
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
        val loadingDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 6f
            centerRadius = 24f
            setColorSchemeColors(Color.WHITE)
            setBounds(0, 0, 100, 100)
        }

        viewModel.isProcessing.observe(viewLifecycleOwner) { isProcessing ->
            binding.upsertPostButton.isEnabled = !isProcessing
            if (isProcessing) {
                binding.upsertPostButton.text = ""
                binding.upsertPostButton.icon = loadingDrawable
                loadingDrawable.start()
            } else {
                loadingDrawable.stop()
                binding.upsertPostButton.icon = null
                binding.upsertPostButton.text = if (viewModel.post.value == null) "Create Post" else "Update Post"
            }
        }
    }

    private fun handleUpsert() {
        val existingPost = viewModel.post.value
        val title = binding.tripTitleEditText.text.toString().trim()
        val mapLink = binding.googleMapsEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val price = binding.priceEditText.text.toString().toIntOrNull() ?: 0
        val author = existingPost?.author ?: Firebase.auth.currentUser?.uid ?: "Anonymous"

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
        } ?: existingPost?.location

        val existingUrls = selectedPhotos.filterIsInstance<String>()
        val newLocalUris = selectedPhotos.filterIsInstance<Uri>()
        val newBitmaps = newLocalUris.mapNotNull { uriToBitmap(it) }

        val post = Post(
            id = existingPost?.id ?: UUID.randomUUID().toString(),
            title = title,
            author = author,
            description = description,
            location = location,
            numberOfDays = currentDuration,
            price = price,
            mapLink = mapLink,
            photos = existingUrls,
            updatedAt = Date()
        )

        viewModel.upsertPost(post, newBitmaps) { success ->
            if (isAdded && activity != null) {
                context?.let { ctx ->
                    if (success) {
                        val message = if (existingPost == null) "Post created successfully!" else "Post updated successfully!"
                        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        val message = if (existingPost == null) "Failed to create post" else "Failed to update post"
                        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
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
            Log.d("UpsertPostFragment", "Error converting URI to bitmap: ${e.message}")
            null
        }
    }

    private fun validateInput(): Boolean {
        val title = binding.tripTitleEditText.text.toString().trim()
        val mapLink = binding.googleMapsEditText.text.toString().trim()
        val location = binding.locationSearchEditText.text.toString().trim()
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

        if (location.isEmpty()) {
            context?.let {
                Toast.makeText(it, "Location is required", Toast.LENGTH_SHORT).show()
            }
            isValid = false
        }

        return isValid
    }

    private fun isGoogleMapsUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.contains("google.com/maps") ||
                lowerUrl.contains("maps.google.com") ||
                lowerUrl.contains("goo.gl/maps") ||
                lowerUrl.contains("maps.app.goo.gl") ||
                lowerUrl.contains("google.co.il/maps")
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
            val errorBg = ContextCompat.getDrawable(ctx, R.drawable.edit_text_error_background)
            val errorColor = ContextCompat.getColor(ctx, R.color.error_red)
            binding.tripTitleLabel.setTextColor(errorColor)
            binding.tripTitleEditText.background = errorBg
            binding.tripTitleError.text = error
            binding.tripTitleError.visibility = View.VISIBLE
        }
    }

    private fun showGoogleMapsError(error: String) {
        context?.let { ctx ->
            val errorBg = ContextCompat.getDrawable(ctx, R.drawable.edit_text_error_background)
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
