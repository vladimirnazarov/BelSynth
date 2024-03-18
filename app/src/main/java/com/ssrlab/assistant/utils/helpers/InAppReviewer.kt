package com.ssrlab.assistant.utils.helpers

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.ssrlab.assistant.R
import com.ssrlab.assistant.ui.chat.ChatActivity
import com.ssrlab.assistant.ui.chat.ChatActivityNew

class InAppReviewer {

    fun askUserForReview(activity: ChatActivity, onSuccess: () -> Unit) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val request = reviewManager.requestReviewFlow()

        request
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    reviewManager.launchReviewFlow(activity, reviewInfo)
                        .addOnCompleteListener { onSuccess() }
                        .addOnFailureListener { showErrorSnack(activity.getBinding().root, it.message.toString()) }
                } else {
                    @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException).errorCode
                    showErrorSnack(activity.getBinding().root, reviewErrorCode.toString())
                }
            }
            .addOnFailureListener {
                showErrorSnack(activity.getBinding().root, it.message.toString())
            }
    }

    fun askUserForReview(activity: ChatActivityNew, onSuccess: () -> Unit) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val request = reviewManager.requestReviewFlow()

        request
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    reviewManager.launchReviewFlow(activity, reviewInfo)
                        .addOnCompleteListener { onSuccess() }
                        .addOnFailureListener { showErrorSnack(activity.getBinding().root, it.message.toString()) }
                } else {
                    @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException).errorCode
                    activity.getChatViewModel().showErrorMessage(reviewErrorCode.toString())
                }
            }

            .addOnFailureListener {
                activity.getChatViewModel().showErrorMessage(it.message.toString())
            }
    }

    private fun showErrorSnack(view: View, errorMessage: String) {
        val snack = Snackbar.make(view, errorMessage, Snackbar.LENGTH_SHORT)
        snack.apply {
            setTextColor(ContextCompat.getColor(context, R.color.snack_text))
            setBackgroundTint(ContextCompat.getColor(context, R.color.error))
            show()
        }
    }
}