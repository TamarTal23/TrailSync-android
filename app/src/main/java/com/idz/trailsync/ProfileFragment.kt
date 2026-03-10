package com.idz.trailsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.squareup.picasso.Picasso
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.idz.trailsync.model.Model
import com.idz.trailsync.model.User

class ProfileFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // todo tamar logout button and function

    }

    private var userInfo: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val profileImageView: ShapeableImageView = view.findViewById(R.id.profileImageView)
        val profileNameTextView: TextView = view.findViewById(R.id.profileNameTextView)
        val editProfileButton: Button = view.findViewById(R.id.editProfileButton)

        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        val auth = Firebase.auth

        auth.currentUser?.email?.let {
            Model.shared.getUserByEmail(it) { user ->
                userInfo = user
                profileNameTextView.text = user?.username
                val url = user?.profilePicture
                if (!url.isNullOrBlank()) {
                    Picasso.get()
                        .load(url)
                        .resize(130, 130)
                        .centerCrop()
                        .placeholder(R.drawable.user_icon_small)
                        .error(R.drawable.user_icon_small)
                        .into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.user_icon_small)
                }
            }
        }

        return view
    }

}
