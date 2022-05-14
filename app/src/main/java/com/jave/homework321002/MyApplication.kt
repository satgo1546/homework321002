package com.jave.homework321002

import android.app.Application
import android.net.Uri

// 最初本应用只能播放预定义视频，但现在可以播放其他来源的视频了，但为了兼容性（迫真）就不改名了
data class PredefinedVideo(
	val id: Int,
	val name: String,
	val description: String,
	val videoPath: String,
	val commentsPath: String,
)

class MyApplication : Application() {
	val videos = mutableListOf(
		// https://developer.android.com/guide/topics/media/media-formats
		PredefinedVideo(
			1,
			"有屏幕的地方就有……",
			"坏苹果",
			"android.resource://${BuildConfig.APPLICATION_ID}/${R.raw.bad_apple}",
			"android.resource://${BuildConfig.APPLICATION_ID}/${R.raw.bad_apple_comments}",
		), // av706
		PredefinedVideo(
			2,
			"还可以往里面添加其他视频",
			"你被骗了",
			"android.resource://${BuildConfig.APPLICATION_ID}/${R.raw.never_gonna_give_you_up}",
			"android.resource://${BuildConfig.APPLICATION_ID}/${R.raw.never_gonna_give_you_up_comments}",
		), // av677985054
	)

	override fun onCreate() {
		super.onCreate()
		filesDir.listFiles { _, name -> name.lowercase().endsWith(".xml") }?.let {
			videos.addAll(it.mapIndexed { i, file ->
				val commentsPath = Uri.fromFile(file).toString()
				PredefinedVideo(
					videos.size + i + 16,
					file.nameWithoutExtension,
					"来自本地",
					commentsPath.removeSuffix(".xml") + ".mp4",
					commentsPath,
				)
			})
		}
	}

}
