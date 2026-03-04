package com.idz.trailsync.features.home.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.idz.trailsync.databinding.FragmentPostDetailsBinding
import com.idz.trailsync.features.home.post.photo.PhotoCarouselController
import com.idz.trailsync.model.Post

class PostDetailsFragment : Fragment() {
    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: PostDetailsFragmentArgs by navArgs()
    private lateinit var photoCarouselController: PhotoCarouselController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)

        val post = args.post
        setupUI(post)

        return binding.root
    }

    private fun setupUI(post: Post) {
        binding.toolbarTitle.text = post.title
        binding.saveCount.text = post.savedCount.toString()
        binding.postDescription.text = post.description
        binding.postRegion.text = post.location?.name ?: "Unknown"
        binding.postDuration.text = "${post.numberOfDays} days"
        binding.postPrice.text = "$${post.price}"

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        photoCarouselController = PhotoCarouselController(
            requireContext(),
            binding.photosRecyclerView,
            binding.dotsIndicator
        )
        photoCarouselController.setupPhotos(post.photos)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
