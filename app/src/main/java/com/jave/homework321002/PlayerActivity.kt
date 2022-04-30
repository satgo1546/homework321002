package com.jave.homework321002

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView

class PlayerActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_player)
		val videoView = findViewById<VideoView>(R.id.videoView)
		val id = intent.extras?.getInt("id") ?: throw IllegalArgumentException("呼叫PlayerActivity时没有指定ID")
		videoView.setVideoPath("android.resource://$packageName/$id")
		videoView.setMediaController(MediaController(this).apply {
			setAnchorView(videoView)
		})
		videoView.start()
	}
}
