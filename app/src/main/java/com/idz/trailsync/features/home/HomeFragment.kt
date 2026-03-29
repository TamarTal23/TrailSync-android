package com.idz.trailsync.features.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.idz.trailsync.R
import com.idz.trailsync.databinding.FragmentHomeBinding
import com.idz.trailsync.shared.location.LocationAutocompleteController
import com.idz.trailsync.features.post.OnPostClickListener
import com.idz.trailsync.features.post.PostsAdapter
import com.idz.trailsync.model.Post
import com.idz.trailsync.shared.viewModels.PostSharedViewModel
import com.idz.trailsync.utils.DialogUtils

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
    private val postSharedViewModel: PostSharedViewModel by activityViewModels()
    private var adapter: PostsAdapter? = null
    private var locationController: LocationAutocompleteController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupSearchAndDrawerTrigger()
        setupDrawerFilters()
        observeViewModel()
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
                navigateToPostDetails(post)
            }

            override fun onDeleteClick(post: Post) {
                DialogUtils.showDeletePostConfirmation(requireContext()) {
                    deletePost(post.id)
                }
            }

            override fun onEditClick(post: Post) {
                val action = HomeFragmentDirections.actionHomeFragmentToUpsertPostFragment(post)
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

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = this@HomeFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItem >= totalItemCount - 2) {
                        viewModel.loadNextPage()
                    }
                }
            })
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
            postSharedViewModel.refreshSavedPosts()
        }
    }

    private fun setupSearchAndDrawerTrigger() {
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)

        binding.btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        locationController = LocationAutocompleteController(
            requireContext(),
            binding.locationFilterEditText,
            binding.locationSuggestionsRecyclerView
        ) { place ->
            viewModel.updateLocation(place.name)
        }

        binding.locationFilterEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    viewModel.updateLocation(null)
                }
            }
        })
    }

    private fun setupDrawerFilters() {
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        val priceEditText = requireActivity().findViewById<EditText>(R.id.drawerPriceEditText)
        val minDaysEditText = requireActivity().findViewById<EditText>(R.id.drawerMinDaysEditText)
        val maxDaysEditText = requireActivity().findViewById<EditText>(R.id.drawerMaxDaysEditText)
        val btnApply = requireActivity().findViewById<MaterialButton>(R.id.btnApplyFiltersDrawer)
        val btnClear = requireActivity().findViewById<MaterialButton>(R.id.btnClearFiltersDrawer)

        btnApply?.setOnClickListener {
            viewModel.applyAdvancedFilters(
                priceEditText?.text?.toString(),
                minDaysEditText?.text?.toString(),
                maxDaysEditText?.text?.toString()
            )
            hideKeyboard()
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnClear?.setOnClickListener {
            priceEditText?.text = null
            minDaysEditText?.text = null
            maxDaysEditText?.text = null
            viewModel.clearFilters()
            hideKeyboard()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun deletePost(postId: String) {
        postSharedViewModel.deletePost(postId) { success ->
            if (success) {
                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter?.posts = posts
            adapter?.notifyDataSetChanged()
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefresh.isRefreshing = isRefreshing
        }

        adapter?.currentUserId = postSharedViewModel.currentUserId
        
        postSharedViewModel.savedPostIds.observe(viewLifecycleOwner) { ids ->
            adapter?.savedPostIds = ids
            adapter?.notifyDataSetChanged()
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
