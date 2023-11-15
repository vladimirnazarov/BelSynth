package com.ssrlab.assistant.ui.login.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentVideoBinding
import com.ssrlab.assistant.ui.login.fragments.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideoFragment: BaseFragment() {

    private lateinit var binding: FragmentVideoBinding
    private lateinit var controller: WindowInsetsControllerCompat

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        controller = WindowInsetsControllerCompat(launchActivity.window, launchActivity.window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentVideoBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        controller.hide(WindowInsetsCompat.Type.systemBars())
        initLogo()
    }

    override fun onStop() {
        super.onStop()

        controller.show(WindowInsetsCompat.Type.systemBars())
    }

    private fun initLogo() {
        if (launchActivity.isFirstLaunch()) {
            binding.apply {

                scope.launch {
                    launchActivity.runOnUiThread {
                        videoIc.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.long_alpha_in))
                        videoIc.visibility = View.VISIBLE
                    }

                    delay(750)

                    launchActivity.runOnUiThread {
                        videoTitle.text = resources.getText(R.string.launch_name)
                        videoTitle.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.long_alpha_in))
                        videoTitle.visibility = View.VISIBLE
                    }
                }

                val videoPath = "android.resource://${launchActivity.packageName}/${R.raw.back}"
                val uri = Uri.parse(videoPath)

                video.visibility = View.VISIBLE
                video.setVideoURI(uri)
                video.setOnPreparedListener {
                    it.playbackParams = it.playbackParams.setSpeed(2.0f)
                    video.start()
                }
                video.setOnCompletionListener {
//                    launchActivity.intentNext()
                    findNavController().navigate(R.id.action_videoFragment_to_loginFragment)
                }
            }
        } else {
            binding.apply {
                videoBg.visibility = View.VISIBLE

                scope.launch {
                    launchActivity.runOnUiThread {
                        videoIc.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.long_alpha_in))
                        videoIc.visibility = View.VISIBLE
                    }

                    delay(750)

                    launchActivity.runOnUiThread {
                        videoTitle.text = resources.getText(R.string.launch_name)
                        videoTitle.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.long_alpha_in))
                        videoTitle.visibility = View.VISIBLE
                    }
                }

                scope.launch {
                    delay(3000)
                    launchActivity.runOnUiThread {
//                        launchActivity.intentNext()
                        findNavController().navigate(R.id.action_videoFragment_to_loginFragment)
                    }
                }
            }
        }
    }
}