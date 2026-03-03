package com.idz.trailsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.features.post_lists.PostsAdapter
import com.idz.trailsync.model.Post
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        recyclerView = view.findViewById(R.id.posts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        adapter = PostsAdapter(getStaticPosts())
        recyclerView.adapter = adapter
        
        return view
    }

    private fun getStaticPosts(): List<Post> {
        return listOf(
            Post(
                id = "1",
                title = "My sheep trip",
                author = "Author 1",
                description = "A beautiful trip with sheep.",
                location = Post.Location(name = "Karineside"),
                numberOfDays = 5,
                price = 800,
                photos = listOf("android.resource://com.idz.trailsync/" + R.drawable.pic1)
            ),
            Post(
                id = "2",
                title = "Mountain Hiking",
                author = "Author 2",
                description = "Exploring the high peaks.",
                location = Post.Location(name = "Alps"),
                numberOfDays = 3,
                price = 450,
                photos = listOf("android.resource://com.idz.trailsync/" + R.drawable.pic2)
            ),
            Post(
                id = "3",
                title = "Beach Relaxation",
                author = "Author 3",
                description = "Sun, sand and sea.",
                location = Post.Location(name = "Maldives"),
                numberOfDays = 7,
                price = 1200,
                photos = listOf("android.resource://com.idz.trailsync/" + R.drawable.pic3)
            )
        )
    }
}
