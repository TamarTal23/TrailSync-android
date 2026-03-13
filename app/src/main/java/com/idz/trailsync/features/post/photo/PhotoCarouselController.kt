package com.idz.trailsync.features.post.photo

import android.content.Context
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

    private val adapter = PhotoAdapter<String>()
    private val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    private val snapHelper = PagerSnapHelper()

    init {
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.clipToPadding = false

        snapHelper.attachToRecyclerView(recyclerView)
        setupScrollListener()
    }

    fun setupPhotos(photos: List<String>) {
        adapter.photos = photos
        adapter.notifyDataSetChanged()

        handlePhotoCentering(photos.size)
        setupDotsIndicator(photos.size)
    }

    private fun handlePhotoCentering(photoCount: Int) {
        val density = context.resources.displayMetrics.density

        val padding = if (photoCount == 1) {
            val screenWidth = context.resources.displayMetrics.widthPixels
            val itemWidth = (290 * density).toInt()
            (screenWidth - itemWidth) / 2
        } else {
            (16 * density).toInt()
        }

        recyclerView.setPadding(padding, 0, padding, 0)
    }

    private fun setupDotsIndicator(photoCount: Int) {
        dotsContainer.removeAllViews()

        if (photoCount <= 1) {
            dotsContainer.visibility = View.GONE
            return
        }

        dotsContainer.visibility = View.VISIBLE
        val density = context.resources.displayMetrics.density
        val margin = (8 * density).toInt()

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(margin, 0, margin, 0)
        }

        for (i in 0 until photoCount) {
            val dot = ImageView(context).apply {
                setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.dot_inactive)
                )
                layoutParams = params
            }

            dotsContainer.addView(dot)
        }

        updateDots(0)
    }

    private fun updateDots(position: Int) {
        for (i in 0 until dotsContainer.childCount) {
            val imageView = dotsContainer.getChildAt(i) as? ImageView

            imageView?.let {
                val drawable = if (i == position) {
                    R.drawable.dot_active
                } else {
                    R.drawable.dot_inactive
                }

                it.setImageDrawable(ContextCompat.getDrawable(context, drawable))
            }
        }
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return

                val centerView = snapHelper.findSnapView(layoutManager) ?: return
                val position = layoutManager.getPosition(centerView)

                updateDots(position)
            }
        })
    }
}