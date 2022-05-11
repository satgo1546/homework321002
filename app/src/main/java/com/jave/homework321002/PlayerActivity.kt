package com.jave.homework321002

import android.animation.TimeAnimator
import android.app.Activity
import android.content.res.Configuration
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.min

// 关于媒体控制器的实现抄自一教程，该教程中的代码又抄自Android本身。
// https://github.com/brightec/ExampleMediaController
class PlayerActivity : AppCompatActivity() {
	private lateinit var root: LinearLayout
	private lateinit var player: VideoView
	private var danmaku: ArrayList<Danmaku> = arrayListOf()
	private lateinit var danmakuView: View
	private lateinit var pauseButton: ImageButton
	private var dragging = false
	private lateinit var mediaControllerProgress: SeekBar
	private lateinit var currentTime: TextView
	private lateinit var endTime: TextView
	private lateinit var animator: TimeAnimator
	private var editing = false
	private lateinit var editingPanel: ViewGroup
	private lateinit var backButton: Button

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
		danmaku = resources.openRawResource(predefinedVideo.commentsResourceId).use {
			Danmaku.listFromXml(it)
		}

		player = findViewById(R.id.videoView)
		player.setVideoPath("android.resource://$packageName/${predefinedVideo.videoResourceId}")
		player.setOnPreparedListener {
			it.start()
			animator.start()
			mediaControllerProgress.max = it.duration
			endTime.text = stringForTime(it.duration)
			updateUi()
		}
		danmakuView = object : View(this) {
			val paint = Paint(Paint.SUBPIXEL_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
				typeface = Typeface.SANS_SERIF
			}
			val rect = Rect()
			override fun onDraw(canvas: Canvas?) {
				super.onDraw(canvas)
				canvas ?: return
				val t = player.currentPosition / 1000f
				val ts = 3f // 视频正常播放时，一条弹幕显示屏幕上的总时长，决定滚动弹幕的速度
				for (i in danmaku.binarySearchBy(t - ts) { it.time }.let {
					if (it < 0) -it - 1 else it
				} until danmaku.size) {
					val d = danmaku[i]
					if (d.time > t) break
					paint.textSize = d.fontSize * min(player.width, player.height) / 480f
					paint.strokeWidth = paint.textSize / 20f
					paint.getTextBounds(d.text, 0, d.text.length, rect)
					val x = when (d.type) {
						Danmaku.Type.SCROLLING -> (width + rect.width()) * (1f - (t - d.time) / ts) - rect.width()
						Danmaku.Type.TOP, Danmaku.Type.BOTTOM -> (width - rect.width()) / 2f
					}
					val y = (i % (height / 2 / rect.height())) * rect.height().toFloat()
					paint.style = Paint.Style.STROKE
					paint.color = Color.BLACK
					canvas.drawText(d.text, x, y, paint)
					paint.style = Paint.Style.FILL
					paint.color = d.color
					canvas.drawText(d.text, x, y, paint)
				}
			}
		}
		findViewById<FrameLayout>(R.id.videoFrame).addView(
			danmakuView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		)

		backButton = findViewById(R.id.button)
		backButton.setOnClickListener {
			this.finish()
		}

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
				player.seekTo(progress)
				currentTime.text = stringForTime(progress)
			}

			override fun onStopTrackingTouch(bar: SeekBar) {
				dragging = false
				updateUi()
			}
		})
		currentTime = findViewById(R.id.time_current)
		endTime = findViewById(R.id.time)

		findViewById<Button>(R.id.leaveEditingButton).setOnClickListener {
			editing = false
			updateUi()
		}
		animator = TimeAnimator().apply {
			setTimeListener { _, _, _ ->
				danmakuView.invalidate()
				if (dragging) return@setTimeListener
				val t = player.currentPosition
				mediaControllerProgress.progress = t
				currentTime.text = stringForTime(t)
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
