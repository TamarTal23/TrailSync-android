package com.idz.trailsync.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
                navigateToPostDetails(post)
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
        binding.swipeRefresh.isRefreshing = false
    }

    private fun navigateToPostDetails(post: Post) {
        val action = HomeFragmentDirections.actionHomeFragmentToPostDetailsFragment(post)
        findNavController().navigate(action)
    }

    private fun getStaticPosts(): List<Post> {
        val packageName = requireContext().packageName
        return listOf(
            Post(
                id = "1",
                title = "My sheep trip",
                author = "Israel Israeli",
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod. Ut enim ad minim veniam. Israel Israeli and his sheep.",
                location = Post.Location(name = "Karineside"),
                numberOfDays = 5,
                price = 800,
                mapLink = "https://www.google.com/maps/d/embed?mid=101CRKWQbwExAnzfudZWBECB4XBGr8Qg&ehbc=2E312F",
                photos = listOf(
                    "android.resource://$packageName/${R.drawable.pic1}",
                    "android.resource://$packageName/${R.drawable.pic2}",
                    "android.resource://$packageName/${R.drawable.pic3}"
                ),
                savedCount = 128,
                commentsCount = 50
            ),
            Post(
                id = "2",
                title = "Mountain Hiking",
                author = "Author 2",
                description = "Exploring the high peaks and beautiful valleys.",
                location = Post.Location(name = "Alps"),
                numberOfDays = 3,
                price = 450,
                mapLink = "https://undraw.co/search",
                photos = listOf("android.resource://$packageName/${R.drawable.pic2}"),
                savedCount = 45,
                commentsCount = 12
            ),
            Post(
                id = "3",
                title = "Beach Relaxation",
                author = "Author 3",
                description = "Sun, sand and crystal clear water.",
                location = Post.Location(name = "Maldives"),
                numberOfDays = 7,
                price = 1200,
                photos = listOf("android.resource://$packageName/${R.drawable.pic3}"),
                savedCount = 230,
                mapLink = "https://www.google.com/maps/place/%D7%A4%D7%A8%D7%99%D7%96,+%D7%A6%D7%A8%D7%A4%D7%AA%E2%80%AD/@48.8589385,2.429435,12z/data=!3m1!4b1!4m6!3m5!1s0x47e66e1f06e2b70f:0x40b82c3688c9460!8m2!3d48.8575475!4d2.3513765!16zL20vMDVxdGo?entry=ttu&g_ep=EgoyMDI2MDMwMy4wIKXMDSoASAFQAw%3D%3D",
                commentsCount = 88
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}