package com.idz.trailsync.features.post.PostDetails

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.idz.trailsync.R
import com.idz.trailsync.databinding.FragmentPostDetailsBinding
import com.idz.trailsync.features.post.photo.PhotoCarouselController
import com.idz.trailsync.model.Post
import com.idz.trailsync.shared.viewModels.AuthenticationViewModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class PostDetailsFragment : Fragment() {
    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: PostDetailsFragmentArgs by navArgs()
    private lateinit var photoCarouselController: PhotoCarouselController
    private lateinit var viewModel: PostDetailsViewModel
    private val authViewModel: AuthenticationViewModel by activityViewModels()
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PostDetailsViewModel::class.java]

        val post = args.post
        setupUI(post)
        setupComments(post.id)
        observeViewModel(post.id)

        viewModel.fetchPostAuthor(post.author)

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

        setupMapView(post.mapLink)
        setupAddCommentSection(post.id)
    }

    private fun setupComments(postId: String) {
        commentAdapter = CommentAdapter()
        binding.commentsRecyclerView.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }

        viewModel.getCommentsForPost(postId).observe(viewLifecycleOwner) { comments ->
            commentAdapter.submitList(comments)
        }
    }

    private fun observeViewModel(postId: String) {
        viewModel.postAuthor.observe(viewLifecycleOwner) { author ->
            _binding?.let { b ->
                b.authorName.text = author?.username ?: "Anonymous"

                if (!author?.profilePicture.isNullOrBlank()) {
                    Picasso.get()
                        .load(author?.profilePicture)
                        .placeholder(R.drawable.user_icon_small)
                        .error(R.drawable.user_icon_small)
                        .fit()
                        .centerCrop()
                        .into(b.authorImage)
                } else {
                    b.authorImage.setImageResource(R.drawable.user_icon_small)
                }
            }
        }

        authViewModel.currentUserProfile.observe(viewLifecycleOwner) { user ->
            _binding?.let { b ->
                if (user != null) {
                    b.addCommentContainer.visibility = View.VISIBLE
                    if (!user.profilePicture.isNullOrEmpty()) {
                        Picasso.get().load(user.profilePicture)
                            .into(b.addCommentLayout.profileImage)
                    }

                    b.addCommentLayout.sendButton.setOnClickListener {
                        val text = b.addCommentLayout.commentInput.text.toString().trim()
                        if (text.isNotEmpty()) {
                            setCommentLoading(true)
                            viewModel.addComment(text, postId, user) { success ->
                                activity?.runOnUiThread {
                                    setCommentLoading(false)
                                    if (success) {
                                        _binding?.addCommentLayout?.commentInput?.text?.clear()
                                        hideKeyboard()
                                    } else {
                                        context?.let { ctx ->
                                            Toast.makeText(
                                                ctx,
                                                "Failed to add comment",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    b.addCommentContainer.visibility = View.GONE
                }
            }
        }
    }

    private fun setCommentLoading(isLoading: Boolean) {
        _binding?.addCommentLayout?.let { layout ->
            if (isLoading) {
                layout.sendButton.visibility = View.INVISIBLE
                layout.commentProgressBar.visibility = View.VISIBLE
                layout.commentInput.isEnabled = false
            } else {
                layout.sendButton.visibility = View.VISIBLE
                layout.commentProgressBar.visibility = View.GONE
                layout.commentInput.isEnabled = true
            }
        }
    }

    private fun setupAddCommentSection(postId: String) {
        binding.addCommentLayout.sendButton.isEnabled = false

        binding.addCommentLayout.commentInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                _binding?.addCommentLayout?.sendButton?.isEnabled = !s.isNullOrBlank()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val inputMethodManager =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }

    private fun isGoogleMapsUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.contains("google.com/maps") ||
                lowerUrl.contains("maps.google.com") ||
                lowerUrl.contains("goo.gl/maps") ||
                lowerUrl.contains("maps.app.goo.gl") ||
                lowerUrl.contains("google.co.il/maps")
    }

    private fun setupMapView(mapLink: String?) {
        if (mapLink.isNullOrEmpty() || !isGoogleMapsUrl(mapLink)) {
            binding.mapContainer.visibility = View.GONE
            return
        }

        binding.mapWebView.apply {
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.domStorageEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    _binding?.mapProgressBar?.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    _binding?.mapProgressBar?.visibility = View.GONE
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        return !isGoogleMapsUrl(url)
                    }

                    val context = view?.context ?: return false
                    return try {
                        val intent = if (url.startsWith("intent://")) {
                            Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        } else {
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        }

                        val isMapIntent =
                            intent.`package`?.contains("com.google.android.apps.maps") == true ||
                                    intent.action?.contains("geo") == true ||
                                    intent.dataString?.let { isGoogleMapsUrl(it) } == true

                        if (isMapIntent && intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            loadUrl(mapLink)
        }
    }

    override fun onDestroyView() {
        _binding?.mapWebView?.stopLoading()
        _binding?.mapWebView?.destroy()
        super.onDestroyView()
        _binding = null
    }
}
