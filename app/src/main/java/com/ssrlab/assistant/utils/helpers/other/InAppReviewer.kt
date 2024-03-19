package com.ssrlab.assistant.utils.helpers.other

import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.ssrlab.assistant.ui.chat.ChatActivity

class InAppReviewer {

    fun askUserForReview(activity: ChatActivity, onSuccess: () -> Unit) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val request = reviewManager.requestReviewFlow()

        val viewModel = activity.getChatViewModel()

        request
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    reviewManager.launchReviewFlow(activity, reviewInfo)
                        .addOnCompleteListener { onSuccess() }
                        .addOnFailureListener { viewModel.showErrorMessage(it.message.toString()) }
                } else {
                    @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException).errorCode
                    viewModel.showErrorMessage(reviewErrorCode.toString())
                }
            }

            .addOnFailureListener {
                viewModel.showErrorMessage(it.message.toString())
            }
    }
}