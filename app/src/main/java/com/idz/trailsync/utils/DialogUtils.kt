package com.idz.trailsync.utils

import android.app.AlertDialog
import android.content.Context

object DialogUtils {
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showDeletePostConfirmation(context: Context, onConfirm: () -> Unit) {
        showConfirmationDialog(
            context,
            "Remove post",
            "Are you sure you want to remove this post?",
            onConfirm
        )
    }
}
