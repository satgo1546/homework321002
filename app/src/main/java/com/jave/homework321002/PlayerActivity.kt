package com.jave.homework321002

import android.animation.TimeAnimator
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

// 关于媒体控制器的实现抄自一教程，该教程中的代码又抄自Android本身。
// https://github.com/brightec/ExampleMediaController
class PlayerActivity : AppCompatActivity() {
	var editing = false
	lateinit var root: LinearLayout
	lateinit var editingPanel: ViewGroup
	lateinit var surfaceView: SurfaceView
	lateinit var player: MediaPlayer
	lateinit var pauseButton: ImageButton
	lateinit var mediaControllerProgress: SeekBar
	lateinit var currentTime: TextView
	lateinit var endTime: TextView
	lateinit var animator: TimeAnimator
	var dragging = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_player)
		root = findViewById(R.id.root)
		editingPanel = findViewById(R.id.editingPanel)
		val predefinedVideo = run {
			val id = intent.extras?.getInt("id") ?: throw IllegalArgumentException("id required")
			PredefinedVideos.find { it.id == id } ?: throw IllegalArgumentException("bad id")
		}
		title = predefinedVideo.name

		surfaceView = findViewById(R.id.surfaceView)
		surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
			override fun surfaceCreated(holder: SurfaceHolder) {
				player.setDisplay(holder)
				player.prepareAsync()
			}

			override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit
			override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
		})

		pauseButton = findViewById(R.id.pause)
		pauseButton.setOnClickListener {
			if (player.isPlaying) player.pause() else player.start()
			updateUi()
		}
		findViewById<ImageButton>(R.id.ffwd).setOnClickListener {
			player.seekTo(player.currentPosition + 15000)
		}
		findViewById<ImageButton>(R.id.rew).setOnClickListener {
			player.seekTo(player.currentPosition - 5000)
		}
		mediaControllerProgress = findViewById(R.id.mediacontroller_progress)
		mediaControllerProgress.max = 1000
		mediaControllerProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			// There are two scenarios that can trigger the seekbar listener to trigger:
			//
			// The first is the user using the touchpad to adjust the position of the
			// seekbar's thumb. In this case onStartTrackingTouch is called followed by
			// a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
			// We're setting the field "mDragging" to true for the duration of the dragging
			// session to avoid jumps in the position in case of ongoing playback.
			//
			// The second scenario involves the user operating the scroll ball, in this
			// case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
			// we will simply apply the updated position without suspending regular updates.
			override fun onStartTrackingTouch(bar: SeekBar) {
				dragging = true
			}

			override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
				if (!fromuser) return
				(player.duration.toLong() * progress / 1000L).toInt().let {
					player.seekTo(it)
					currentTime.text = stringForTime(it)
				}
			}

			override fun onStopTrackingTouch(bar: SeekBar) {
				dragging = false
				updateUi()
			}
		})
		currentTime = findViewById(R.id.time_current)
		endTime = findViewById(R.id.time)

		player = MediaPlayer().apply {
			setDataSource(resources.openRawResourceFd(predefinedVideo.resourceId))
			setOnPreparedListener {
				start()
				animator.start()
				updateUi()
			}
		}
		findViewById<Button>(R.id.leaveEditingButton).setOnClickListener {
			editing = false
			updateUi()
		}
		animator = TimeAnimator().apply {
			setTimeListener { _, _, _ ->
				if (dragging) return@setTimeListener
				val position = player.currentPosition
				val duration = player.duration
				if (duration > 0) {
					// use long to avoid overflow
					val pos = 1000L * position / duration
					mediaControllerProgress.progress = pos.toInt()
				}
				currentTime.text = stringForTime(position)
				endTime.text = stringForTime(duration)
			}
		}
		updateUi()
	}

	override fun onPause() {
		super.onPause()
		player.pause()
		updateUi()
	}

	fun updateUi() {
		if (editing) {
			editingPanel.visibility = View.VISIBLE
			supportActionBar?.hide()
		} else {
			editingPanel.visibility = View.GONE
			supportActionBar?.show()
		}
		root.orientation = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			LinearLayout.HORIZONTAL
		} else {
			LinearLayout.VERTICAL
		}
		pauseButton.setImageResource(
			if (player.isPlaying) {
				android.R.drawable.ic_media_pause
			} else {
				android.R.drawable.ic_media_play
			}
		)
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		super.onCreateOptionsMenu(menu)
		menu?.add("编辑")?.apply {
			setIcon(android.R.drawable.ic_menu_edit)
			setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
			setOnMenuItemClickListener {
				editing = true
				updateUi()
				true
			}
		}
		return true
	}

	fun stringForTime(timeMs: Int): String {
		val totalSeconds = timeMs / 1000
		val seconds = totalSeconds % 60
		val minutes = totalSeconds / 60 % 60
		val hours = totalSeconds / 3600
		return if (hours > 0) {
			String.format("%d:%02d:%02d", hours, minutes, seconds)
		} else {
			String.format("%02d:%02d", minutes, seconds)
		}
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		updateUi()
	}
}
