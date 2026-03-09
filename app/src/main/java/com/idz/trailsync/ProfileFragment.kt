package com.idz.trailsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val editProfileButton: Button = view.findViewById(R.id.editProfileButton)
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }
        return view
    }
}
