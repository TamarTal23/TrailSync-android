package com.idz.trailsync.features.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.idz.trailsync.R
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.databinding.FragmentHomeBinding
import com.idz.trailsync.features.post.OnPostClickListener
import com.idz.trailsync.features.post.PostsAdapter
import com.idz.trailsync.model.Post

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private var adapter: PostsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)

        adapter = PostsAdapter(viewModel.posts.value)
        adapter?.listener = object : OnPostClickListener {
            override fun onPostClick(post: Post) {
                navigateToPostDetails(post)
            }

            override fun onDeleteClick(post: Post) {
                showDeleteConfirmationDialog(post)
            }
        }
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }

        observePosts()
    }

    private fun showDeleteConfirmationDialog(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove post")
            .setMessage("Are you sure you want to remove this post?")
            .setPositiveButton("Yes") { _, _ ->
                deletePost(post)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(post: Post) {
        PostRepository.shared.deletePost(post.id) { success ->
            if (success) {
                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                refreshData()
            } else {
                Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observePosts() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter?.posts = posts
            adapter?.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun refreshData() {
        viewModel.refreshPosts()
    }

    private fun navigateToPostDetails(post: Post) {
        val action = HomeFragmentDirections.actionHomeFragmentToPostDetailsFragment(post)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
