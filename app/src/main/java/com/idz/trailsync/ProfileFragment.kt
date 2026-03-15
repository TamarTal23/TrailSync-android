package com.idz.trailsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.data.repository.UserRepository
import com.idz.trailsync.databinding.FragmentProfileBinding
import com.idz.trailsync.features.post.OnPostClickListener
import com.idz.trailsync.features.post.PostsAdapter
import com.idz.trailsync.model.Post
import com.idz.trailsync.model.User
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var userInfo: User? = null
    
    private val viewModel: ProfileViewModel by viewModels()
    private var adapter: PostsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        binding.profileSwipeRefresh.setOnRefreshListener {
            refreshData()
        }

        getUserData()
        observeUserPosts()
    }

    private fun setupRecyclerView() {
        binding.userPostsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PostsAdapter(viewModel.userPosts.value)
        adapter?.listener = object : OnPostClickListener {
            override fun onPostClick(post: Post) {
                val action = ProfileFragmentDirections.actionProfileFragmentToPostDetailsFragment(post)
                findNavController().navigate(action)
            }
        }
        binding.userPostsRecyclerView.adapter = adapter
    }

    private fun getUserData() {
        val currentUserId = Firebase.auth.currentUser?.uid

        currentUserId?.let { uid ->
            UserRepository.shared.getUserById(uid) { user ->
                _binding?.let { b ->
                    userInfo = user
                    b.profileNameTextView.text = user?.username
                    loadProfileImage(user?.profilePicture)
                }
            }
        }
    }

    private fun observeUserPosts() {
        val currentUserId = Firebase.auth.currentUser?.uid

        currentUserId?.let { uid ->
            if (viewModel.userPosts.value == null) {
                viewModel.refreshUserPosts(uid)
            }

            viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
                adapter?.posts = posts
                adapter?.notifyDataSetChanged()
                binding.profileSwipeRefresh.isRefreshing = false
            }
        }
    }

    private fun refreshData() {
        val currentUserId = Firebase.auth.currentUser?.uid
        currentUserId?.let { uid ->
            viewModel.refreshUserPosts(uid)
        } ?: run {
            binding.profileSwipeRefresh.isRefreshing = false
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
            .into(binding.profileImageView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    binding.profileProgressBar.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    binding.profileProgressBar.visibility = View.GONE
                    binding.profileImageView.setImageResource(R.drawable.user_icon_small)
                    Toast.makeText(context, "Error loading profile image", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
