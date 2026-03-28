package com.idz.trailsync.features.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.idz.trailsync.R
import com.idz.trailsync.databinding.FragmentProfileBinding
import com.idz.trailsync.features.post.OnPostClickListener
import com.idz.trailsync.features.post.PostsAdapter
import com.idz.trailsync.model.Post
import com.idz.trailsync.shared.viewModels.AuthenticationViewModel
import com.idz.trailsync.shared.viewModels.PostSharedViewModel
import com.idz.trailsync.utils.DialogUtils
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthenticationViewModel by activityViewModels()
    private val postSharedViewModel: PostSharedViewModel by activityViewModels()

    private var adapter: PostsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        authViewModel.currentUserId.observe(viewLifecycleOwner) { uid ->
            uid?.let {
                viewModel.setUserId(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPosts()
        postSharedViewModel.refreshSavedPosts()
    }

    private fun setupRecyclerView() {
        adapter = PostsAdapter()
        adapter?.listener = object : OnPostClickListener {
            override fun onPostClick(post: Post) {
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToPostDetailsFragment(post)
                findNavController().navigate(action)
            }

            override fun onDeleteClick(post: Post) {
                DialogUtils.showDeletePostConfirmation(requireContext()) {
                    deletePost(post)
                }
            }

            override fun onEditClick(post: Post) {
                val action = ProfileFragmentDirections.actionProfileFragmentToUpsertPostFragment(post)
                findNavController().navigate(action)
            }

            override fun onSaveClick(post: Post) {
                postSharedViewModel.toggleSavePost(post) { success ->
                    if (!success) {
                        Toast.makeText(context, "Failed to update saved status", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.userPostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ProfileFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.profileSwipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
            postSharedViewModel.refreshSavedPosts()
        }
    }

    private fun deletePost(post: Post) {
        postSharedViewModel.deletePost(post.id) { success ->
            if (success) {
                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            adapter?.posts = posts
            adapter?.notifyDataSetChanged()
            binding.profileSwipeRefresh.isRefreshing = false
        }

        authViewModel.currentUserProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.profileNameTextView.text = it.username
                loadProfileImage(it.profilePicture)
            }
        }

        adapter?.currentUserId = postSharedViewModel.currentUserId

        postSharedViewModel.savedPostIds.observe(viewLifecycleOwner) { ids ->
            adapter?.savedPostIds = ids
            adapter?.notifyDataSetChanged()
        }
    }

    private fun loadProfileImage(url: String?) {
        if (url.isNullOrBlank()) {
            binding.profileImageView.setImageResource(R.drawable.user_icon_small)
            return
        }

        binding.profileProgressBar.visibility = View.VISIBLE
        Picasso.get()
            .load(url)
            .resize(240, 240)
            .centerCrop()
            .into(binding.profileImageView, object : Callback {
                override fun onSuccess() {
                    binding.profileProgressBar.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    binding.profileProgressBar.visibility = View.GONE
                    binding.profileImageView.setImageResource(R.drawable.user_icon_small)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
