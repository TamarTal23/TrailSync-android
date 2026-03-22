package com.idz.trailsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.idz.trailsync.databinding.FragmentSavedPostsBinding
import com.idz.trailsync.features.post.OnPostClickListener
import com.idz.trailsync.features.saved.SavedPostViewModel
import com.idz.trailsync.features.saved.SavedPostsAdapter
import com.idz.trailsync.model.Post

class SavedPostsFragment : Fragment() {
    private var _binding: FragmentSavedPostsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedPostViewModel by viewModels()
    private lateinit var adapter: SavedPostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SavedPostsAdapter(null, object : OnPostClickListener {
            override fun onPostClick(post: Post) {
                val action =
                    SavedPostsFragmentDirections.actionSavedPostsFragmentToPostDetailsFragment(post)
                findNavController().navigate(action)
            }

            override fun onDeleteClick(post: Post) {
                viewModel.refreshSavedPosts()
            }

            override fun onEditClick(post: Post) {
            }
        })

        binding.recyclerViewSavedPosts.adapter = adapter

        viewModel.savedPosts.observe(viewLifecycleOwner) { posts ->
            if (posts.isNullOrEmpty()) {
                binding.textNoSavedPosts.visibility = View.VISIBLE
                binding.recyclerViewSavedPosts.visibility = View.GONE
            } else {
                binding.textNoSavedPosts.visibility = View.GONE
                binding.recyclerViewSavedPosts.visibility = View.VISIBLE
                adapter.posts = posts
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.refreshSavedPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
