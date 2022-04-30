package com.jave.homework321002

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

// 关于媒体控制器的实现抄自一教程，该教程中的代码又抄自Android本身。
// https://github.com/brightec/ExampleMediaController
class PlayerActivity : AppCompatActivity() {
	var editing = false
	lateinit var editingPanel: ViewGroup
	lateinit var surfaceView: SurfaceView
	lateinit var mediaController: ViewGroup
	lateinit var player: MediaPlayer
	lateinit var pauseButton: ImageButton
	lateinit var mediaControllerProgress: SeekBar
	lateinit var currentTime: TextView
	lateinit var endTime: TextView
	var isShowing = false
	var dragging = false
	val handler: Handler = MessageHandler(this)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_player)
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

		mediaController = findViewById(R.id.mediaController)
		pauseButton = findViewById(R.id.pause)
		pauseButton.setOnClickListener {
			if (player.isPlaying) player.pause() else player.start()
			updatePausePlay()
			showMediaController()
		}
		findViewById<ImageButton>(R.id.ffwd).setOnClickListener {
			player.seekTo(player.currentPosition + 15000)
			updateMediaProgress()
			showMediaController()
		}
		findViewById<ImageButton>(R.id.rew).setOnClickListener {
			player.seekTo(player.currentPosition - 5000)
			updateMediaProgress()
			showMediaController()
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
				showMediaController(999999999)
				dragging = true

				// By removing these pending progress messages we make sure
				// that a) we won't update the progress while the user adjusts
				// the seekbar and b) once the user is done dragging the thumb
				// we will post one of these messages to the queue again and
				// this ensures that there will be exactly one message queued up.
				handler.removeMessages(SHOW_PROGRESS)
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
				updateMediaProgress()
				updatePausePlay()
				showMediaController()

				// Ensure that progress is properly updated in the future,
				// the call to show() does not guarantee this because it is a
				// no-op if we are already showing.
				handler.sendEmptyMessage(SHOW_PROGRESS)
			}
		})
		currentTime = findViewById(R.id.time_current)
		endTime = findViewById(R.id.time)

		player = MediaPlayer().apply {
			setDataSource(resources.openRawResourceFd(predefinedVideo.resourceId))
			setOnPreparedListener {
				start()
			}
		}
		findViewById<Button>(R.id.leaveEditingButton).setOnClickListener {
			leaveEditing()
		}
		leaveEditing()
	}

	override fun onPause() {
		super.onPause()
		player.pause()
		updatePausePlay()
	}

	fun enterEditing() {
		editing = true
		editingPanel.visibility = View.VISIBLE
		supportActionBar?.hide()
		showMediaController(999999999)
	}

	fun leaveEditing() {
		editing = false
		editingPanel.visibility = View.GONE
		supportActionBar?.show()
		hideMediaController()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		super.onCreateOptionsMenu(menu)
		menu?.add("编辑")?.apply {
			setIcon(android.R.drawable.ic_menu_edit)
			setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
			setOnMenuItemClickListener {
				enterEditing()
				true
			}
		}
		return true
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		showMediaController()
		return false
	}

	override fun onTrackballEvent(event: MotionEvent?): Boolean {
		showMediaController()
		return false
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

	fun showMediaController(timeout: Int = 3000) {
		if (!isShowing) {
			updateMediaProgress()
			mediaController.visibility = View.VISIBLE
			isShowing = true
		}
		updatePausePlay()

		// cause the progress bar to be updated even if mShowing
		// was already true.  This happens, for example, if we're
		// paused with the progress bar showing the user hits play.
		handler.sendEmptyMessage(SHOW_PROGRESS)
		handler.removeMessages(FADE_OUT)
		handler.sendMessageDelayed(handler.obtainMessage(FADE_OUT), timeout.toLong())
	}

	fun hideMediaController() {
		if (editing) return
		mediaController.visibility = View.GONE
		handler.removeMessages(SHOW_PROGRESS)
		isShowing = false
	}

	private fun updateMediaProgress(): Int {
		if (dragging) return 0
		val position = player.currentPosition
		val duration = player.duration
		if (duration > 0) {
			// use long to avoid overflow
			val pos = 1000L * position / duration
			mediaControllerProgress.progress = pos.toInt()
		}
		mediaControllerProgress.secondaryProgress = 500
		currentTime.text = stringForTime(position)
		endTime.text = stringForTime(duration)
		return position
	}

	override fun dispatchKeyEvent(event: KeyEvent): Boolean {
		val uniqueDown = (event.repeatCount == 0 && event.action == KeyEvent.ACTION_DOWN)
		when (event.keyCode) {
			KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_SPACE -> {
				if (uniqueDown) {
					pauseButton.callOnClick()
					showMediaController()
				}
				return true
			}
			KeyEvent.KEYCODE_MEDIA_PLAY -> {
				if (uniqueDown && !player.isPlaying) {
					player.start()
					updatePausePlay()
					showMediaController()
				}
				return true
			}
			KeyEvent.KEYCODE_MEDIA_STOP, KeyEvent.KEYCODE_MEDIA_PAUSE -> {
				if (uniqueDown && player.isPlaying) {
					player.pause()
					updatePausePlay()
					showMediaController()
				}
				return true
			}
			KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_MUTE -> {
				// don't show the controls for volume adjustment
				return super.dispatchKeyEvent(event)
			}
		}
		showMediaController()
		return super.dispatchKeyEvent(event)
	}

	fun updatePausePlay() {
		pauseButton.setImageResource(if (player.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
	}

	private class MessageHandler(private val activity: PlayerActivity) : Handler() {
		override fun handleMessage(msg: Message) {
			when (msg.what) {
				FADE_OUT -> activity.hideMediaController()
				SHOW_PROGRESS -> {
					val pos = activity.updateMediaProgress()
					if (!activity.dragging && activity.isShowing && activity.player.isPlaying) {
						sendMessageDelayed(obtainMessage(SHOW_PROGRESS), (1000 - pos % 1000).toLong())
					}
				}
			}
		}
	}

	companion object {
		private const val FADE_OUT = 1
		private const val SHOW_PROGRESS = 2
	}
}
