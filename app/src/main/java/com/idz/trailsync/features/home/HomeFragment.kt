package com.idz.trailsync.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.idz.trailsync.databinding.FragmentHomeBinding
import com.idz.trailsync.features.post.OnPostClickListener
import com.idz.trailsync.features.post.PostsAdapter
import com.idz.trailsync.model.Post
import com.idz.trailsync.utils.DialogUtils

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
        setupSwipeRefresh()
        observePosts()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPosts()
    }

    private fun setupRecyclerView() {
        adapter = PostsAdapter()
        adapter?.listener = object : OnPostClickListener {
            override fun onPostClick(post: Post) {
                navigateToPostDetails(post)
            }

            override fun onDeleteClick(post: Post) {
                DialogUtils.showDeletePostConfirmation(requireContext()) {
                    deletePost(post)
                }
            }

            override fun onEditClick(post: Post) {
                val action = HomeFragmentDirections.actionHomeFragmentToUpsertPostFragment(post)
                findNavController().navigate(action)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = this@HomeFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }
    }

    private fun deletePost(post: Post) {
        viewModel.deletePost(post.id) { success ->
            if (success) {
                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
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

    private fun navigateToPostDetails(post: Post) {
        val action = HomeFragmentDirections.actionHomeFragmentToPostDetailsFragment(post)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
