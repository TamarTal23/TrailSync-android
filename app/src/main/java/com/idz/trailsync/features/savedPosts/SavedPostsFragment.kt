package com.idz.trailsync.features.savedPosts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.idz.trailsync.databinding.FragmentSavedPostsBinding
import com.idz.trailsync.features.post.OnPostClickListener
import com.idz.trailsync.model.Post
import com.idz.trailsync.shared.viewModels.PostSharedViewModel

class SavedPostsFragment : Fragment() {
    private var _binding: FragmentSavedPostsBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: PostSharedViewModel by activityViewModels()
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
                sharedViewModel.refreshSavedPosts()
            }

            override fun onEditClick(post: Post) {
            }

            override fun onSaveClick(post: Post) {
                sharedViewModel.toggleSavePost(post) { }
            }
        })

        binding.recyclerViewSavedPosts.adapter = adapter

        sharedViewModel.savedPosts.observe(viewLifecycleOwner) { posts ->
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

        sharedViewModel.refreshSavedPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
