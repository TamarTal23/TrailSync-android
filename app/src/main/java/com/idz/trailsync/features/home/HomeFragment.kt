package com.idz.trailsync.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.idz.trailsync.features.home.HomeViewModel
import com.idz.trailsync.R
import com.idz.trailsync.databinding.FragmentHomeBinding
import com.idz.trailsync.features.home.post.OnPostClickListener
import com.idz.trailsync.features.home.post.PostsAdapter
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
                // Navigate to post details if needed
            }
        }
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }

        observePosts()
    }

    private fun observePosts() {
        if (viewModel.posts.value == null) {
            viewModel.setPosts(getStaticPosts())
        }

        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter?.posts = posts
            adapter?.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun refreshData() {
        viewModel.refreshPosts()
        // Simulate refresh completion for static data
        binding.swipeRefresh.isRefreshing = false
    }

    private fun getStaticPosts(): List<Post> {
        val packageName = requireContext().packageName
        return listOf(
            Post(
                id = "1",
                title = "My sheep trip",
                author = "Author 1",
                location = Post.Location(name = "Karineside"),
                numberOfDays = 5,
                price = 800,
                photos = listOf("android.resource://$packageName/${R.drawable.pic1}"),
                savedCount = 128,
                commentsCount = 50
            ),
            Post(
                id = "2",
                title = "Mountain Hiking",
                author = "Author 2",
                location = Post.Location(name = "Alps"),
                numberOfDays = 3,
                price = 450,
                photos = listOf("android.resource://$packageName/${R.drawable.pic2}"),
                savedCount = 45,
                commentsCount = 12
            ),
            Post(
                id = "3",
                title = "Beach Relaxation",
                author = "Author 3",
                location = Post.Location(name = "Maldives"),
                numberOfDays = 7,
                price = 1200,
                photos = listOf("android.resource://$packageName/${R.drawable.pic3}"),
                savedCount = 230,
                commentsCount = 88
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}