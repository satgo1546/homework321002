package com.jave.homework321002

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class PlayerActivity : AppCompatActivity() {
	var editing = false
	lateinit var editingPanel: ViewGroup
	lateinit var videoView: VideoView
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_player)
		editingPanel = findViewById(R.id.editingPanel)
		videoView = findViewById(R.id.videoView)
		val id = intent.extras?.getInt("id") ?: throw IllegalArgumentException("呼叫PlayerActivity时没有指定ID")
		videoView.setVideoPath("android.resource://$packageName/$id")
		val mediaController = object : MediaController(this) {
			override fun hide() {
				if (!editing) super.hide()
			}

			override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
				(context as Activity).finish()
				if (editing) {
					if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
						leaveEditing()
					}
				} else {
					(context as Activity).finish()
				}
				return false
			}
		}
		mediaController.setAnchorView(videoView)
		videoView.setMediaController(mediaController)
		findViewById<Button>(R.id.leaveEditingButton).setOnClickListener {
			leaveEditing()
		}
		videoView.setOnPreparedListener {
			it.start()
			leaveEditing()
		}
	}

	fun enterEditing() {
		if (editing) return
		editing = true
		editingPanel.visibility = View.VISIBLE
		supportActionBar?.hide()
	}

	fun leaveEditing() {
		if (!editing) return
		editing = false
		editingPanel.visibility = View.GONE
		supportActionBar?.show()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		super.onCreateOptionsMenu(menu)
		menu?.add("编辑")?.setOnMenuItemClickListener {
			enterEditing()
			true
		}
		return true
	}
}
