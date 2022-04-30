package com.jave.homework321002

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

// https://github.com/brightec/ExampleMediaController
class PlayerActivity : AppCompatActivity() {
	var editing = false
	lateinit var editingPanel: ViewGroup
	lateinit var surfaceView: SurfaceView
	lateinit var mediaController: ViewGroup
	lateinit var player: MediaPlayer
	lateinit var mProgress: SeekBar
	lateinit var mEndTime: TextView
	lateinit var mCurrentTime: TextView
	var isShowing = false
	var mDragging = false
	lateinit var mPauseButton: ImageButton
	lateinit var mFfwdButton: ImageButton
	lateinit var mRewButton: ImageButton
	val mHandler: Handler = MessageHandler(this)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_player)
		editingPanel = findViewById(R.id.editingPanel)
		val id = intent.extras?.getInt("id") ?: throw IllegalArgumentException("呼叫PlayerActivity时没有指定ID")
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
		mPauseButton = findViewById<View>(R.id.pause) as ImageButton
		mPauseButton.setOnClickListener {
			doPauseResume()
			show()
		}
		mFfwdButton = findViewById<View>(R.id.ffwd) as ImageButton
		mFfwdButton.setOnClickListener {
			player.seekTo(player.currentPosition + 15000)
			setProgress()
			show()
		}
		mRewButton = findViewById(R.id.rew)
		mRewButton.setOnClickListener {
			player.seekTo(player.currentPosition - 5000)
			setProgress()
			show()
		}
		mProgress = findViewById(R.id.mediacontroller_progress)
		mProgress.setOnSeekBarChangeListener(
			// There are two scenarios that can trigger the seekbar listener to trigger:
			//
			// The first is the user using the touchpad to adjust the posititon of the
			// seekbar's thumb. In this case onStartTrackingTouch is called followed by
			// a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
			// We're setting the field "mDragging" to true for the duration of the dragging
			// session to avoid jumps in the position in case of ongoing playback.
			//
			// The second scenario involves the user operating the scroll ball, in this
			// case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
			// we will simply apply the updated position without suspending regular updates.
			object : SeekBar.OnSeekBarChangeListener {
				override fun onStartTrackingTouch(bar: SeekBar) {
					show(3600000)
					mDragging = true

					// By removing these pending progress messages we make sure
					// that a) we won't update the progress while the user adjusts
					// the seekbar and b) once the user is done dragging the thumb
					// we will post one of these messages to the queue again and
					// this ensures that there will be exactly one message queued up.
					mHandler.removeMessages(SHOW_PROGRESS)
				}

				override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
					if (!fromuser) {
						// We're not interested in programmatically generated changes to
						// the progress bar's position.
						return
					}
					val newposition = (player.duration.toLong() * progress / 1000L).toInt()
					player.seekTo(newposition)
					mCurrentTime.text = stringForTime(newposition)
				}

				override fun onStopTrackingTouch(bar: SeekBar) {
					mDragging = false
					setProgress()
					updatePausePlay()
					show()

					// Ensure that progress is properly updated in the future,
					// the call to show() does not guarantee this because it is a
					// no-op if we are already showing.
					mHandler.sendEmptyMessage(SHOW_PROGRESS)
				}
			})
		mProgress.max = 1000
		mEndTime = findViewById<View>(R.id.time) as TextView
		mCurrentTime = findViewById<View>(R.id.time_current) as TextView

		player = MediaPlayer().apply {
			setDataSource(resources.openRawResourceFd(id))
			setOnPreparedListener {
				start()
			}
		}
		findViewById<Button>(R.id.leaveEditingButton).setOnClickListener {
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

	override fun onTouchEvent(event: MotionEvent): Boolean {
		show()
		return false
	}

	override fun onTrackballEvent(event: MotionEvent?): Boolean {
		show()
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

	fun show(timeout: Int = 3000) {
		if (!isShowing) {
			setProgress()
			mPauseButton.requestFocus()
			mediaController.visibility = View.VISIBLE
			isShowing = true
		}
		updatePausePlay()

		// cause the progress bar to be updated even if mShowing
		// was already true.  This happens, for example, if we're
		// paused with the progress bar showing the user hits play.
		mHandler.sendEmptyMessage(SHOW_PROGRESS)
		val msg = mHandler.obtainMessage(FADE_OUT)
		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT)
			mHandler.sendMessageDelayed(msg, timeout.toLong())
		}
	}

	fun hide() {
		mediaController.visibility = View.GONE
		mHandler.removeMessages(SHOW_PROGRESS)
		isShowing = false
	}

	private fun setProgress(): Int {
		if (mDragging) {
			return 0
		}
		val position = player.currentPosition
		val duration = player.duration
		if (duration > 0) {
			// use long to avoid overflow
			val pos = 1000L * position / duration
			mProgress.progress = pos.toInt()
		}
		mProgress.secondaryProgress = 500
		mEndTime.text = stringForTime(duration)
		mCurrentTime.text = stringForTime(position)
		return position
	}

	override fun dispatchKeyEvent(event: KeyEvent): Boolean {
		val keyCode = event.keyCode
		val uniqueDown = (event.repeatCount == 0 && event.action == KeyEvent.ACTION_DOWN)
		if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE) {
			if (uniqueDown) {
				doPauseResume()
				show()
				mPauseButton.requestFocus()
			}
			return true
		} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
			if (uniqueDown && !player.isPlaying) {
				player.start()
				updatePausePlay()
				show()
			}
			return true
		} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
			if (uniqueDown && player.isPlaying) {
				player.pause()
				updatePausePlay()
				show()
			}
			return true
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
			// don't show the controls for volume adjustment
			return super.dispatchKeyEvent(event)
		} else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
			if (uniqueDown) {
				hide()
			}
			return true
		}
		show()
		return super.dispatchKeyEvent(event)
	}

	fun updatePausePlay() {
		if (player.isPlaying) {
			mPauseButton.setImageResource(android.R.drawable.ic_media_pause)
		} else {
			mPauseButton.setImageResource(android.R.drawable.ic_media_play)
		}
	}

	private fun doPauseResume() {
		if (player.isPlaying) {
			player.pause()
		} else {
			player.start()
		}
		updatePausePlay()
	}

	private class MessageHandler(private val activity: PlayerActivity) : Handler() {
		override fun handleMessage(msg: Message) {
			var msg = msg
			val pos: Int
			when (msg.what) {
				FADE_OUT -> activity.hide()
				SHOW_PROGRESS -> {
					pos = activity.setProgress()
					if (!activity.mDragging && activity.isShowing && activity.player.isPlaying) {
						msg = obtainMessage(SHOW_PROGRESS)
						sendMessageDelayed(msg, (1000 - pos % 1000).toLong())
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
