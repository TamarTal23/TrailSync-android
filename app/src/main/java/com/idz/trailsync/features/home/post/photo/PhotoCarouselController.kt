package com.idz.trailsync.features.home.post.photo

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.R

class PhotoCarouselController(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val dotsContainer: LinearLayout
) {
    private val adapter = PhotoAdapter()
    private val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    private val snapHelper = PagerSnapHelper()

    init {
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        snapHelper.attachToRecyclerView(recyclerView)
        setupScrollListener()
    }

    fun setupPhotos(photos: List<String>) {
        adapter.photos = photos
        adapter.notifyDataSetChanged()

        handlePhotoCentering(photos.size)
        setupDotsIndicator(photos.size)
    }

    private fun handlePhotoCentering(size: Int) {
        if (size == 1) {
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val itemWidth = (290 * Resources.getSystem().displayMetrics.density).toInt()
            val padding = (screenWidth - itemWidth) / 2
            recyclerView.setPadding(padding, 0, padding, 0)
        } else {
            val padding = (16 * Resources.getSystem().displayMetrics.density).toInt()
            recyclerView.setPadding(padding, 0, padding, 0)
        }
        recyclerView.clipToPadding = false
    }

    private fun setupDotsIndicator(size: Int) {
        dotsContainer.removeAllViews()
        if (size <= 1) {
            dotsContainer.visibility = View.GONE
            return
        }

        dotsContainer.visibility = View.VISIBLE
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }

        for (i in 0 until size) {
            val dot = ImageView(context).apply {
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dot_inactive))
                layoutParams = params
            }
            dotsContainer.addView(dot)
        }
        updateDots(0)
    }

    private fun updateDots(position: Int) {
        val childCount = dotsContainer.childCount
        for (i in 0 until childCount) {
            val imageView = dotsContainer.getChildAt(i) as? ImageView
            imageView?.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
                )
            )
        }
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(layoutManager)
                    centerView?.let {
                        val position = layoutManager.getPosition(it)
                        updateDots(position)
                    }
                }
            }
        })
    }
}
