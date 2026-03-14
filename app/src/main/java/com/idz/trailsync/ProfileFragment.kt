package com.idz.trailsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.databinding.FragmentProfileBinding
import com.idz.trailsync.model.Model
import com.idz.trailsync.model.User
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var userInfo: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        val auth = Firebase.auth
        val currentUserId = auth.currentUser?.uid

        currentUserId?.let { uid ->
            Model.shared.getUserById(uid) { user ->
                // Check if binding is still available before updating UI
                _binding?.let { b ->
                    userInfo = user
                    b.profileNameTextView.text = user?.username

                    user?.profilePicture?.let { profileUrl ->
                        if (profileUrl.isNotBlank()) {
                            b.profileProgressBar.visibility = View.VISIBLE

                            Picasso.get()
                                .load(profileUrl)
                                .resize(240, 240)
                                .centerCrop()
                                .into(b.profileImageView, object : com.squareup.picasso.Callback {
                                    override fun onSuccess() {
                                        _binding?.profileProgressBar?.visibility = View.GONE
                                    }

                                    override fun onError(e: Exception?) {
                                        _binding?.profileProgressBar?.visibility = View.GONE
                                    }
                                })
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
