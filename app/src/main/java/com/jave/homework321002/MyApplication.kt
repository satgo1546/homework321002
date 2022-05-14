package com.jave.homework321002

import android.app.Application
import android.content.Context
import androidx.core.content.FileProvider
import java.io.File

// 最初本应用只能播放预定义视频，但现在可以播放其他来源的视频了，但为了兼容性（迫真）就不改名了
data class PredefinedVideo(
	val id: Int,
	// name是显示名称，同时也是文件基名：附加.txt获得描述文件，附加.xml获得弹幕文件，附加.mp4获得视频文件。
	val name: String,
	val description: String,
) {
	fun getVideoURI(context: Context) = FileProvider.getUriForFile(
		context, "${context.packageName}.fileprovider", File(context.filesDir, "$name.mp4")
	)
}

class MyApplication : Application() {
	val videos = mutableListOf<PredefinedVideo>()

	override fun onCreate() {
		super.onCreate()
		// 首次运行时，将原始资源复制到私有存储目录。可能会比较慢，请忍耐一下！
		arrayOf(
			// https://developer.android.com/guide/topics/media/media-formats
			Triple(
				PredefinedVideo(0, "有屏幕的地方就有……", "坏苹果"), // av706
				R.raw.bad_apple,
				R.raw.bad_apple_comments,
			),
			Triple(
				PredefinedVideo(0, "还可以往里面添加其他视频", "你被骗了"), // av677985054
				R.raw.never_gonna_give_you_up,
				R.raw.never_gonna_give_you_up_comments,
			),
		).forEach { (predefinedVideo, videoResourceId, commentsResourceId) ->
			File(filesDir, "${predefinedVideo.name}.txt").let {
				if (it.exists()) return@forEach
				it.writeText(predefinedVideo.description)
			}
			File(filesDir, "${predefinedVideo.name}.xml").writeBytes(resources.openRawResource(commentsResourceId)
				.use { inputStream -> inputStream.readBytes() })
			File(filesDir, "${predefinedVideo.name}.mp4").outputStream().use { outputStream ->
				resources.openRawResource(videoResourceId).use { inputStream ->
					inputStream.copyTo(outputStream)
				}
			}
		}
		rescanVideos()
	}

	fun rescanVideos() {
		videos.clear()
		filesDir.listFiles { _, name -> name.lowercase().endsWith(".txt") }?.let {
			videos.addAll(it.mapIndexed { i, file ->
				PredefinedVideo(
					i,
					file.nameWithoutExtension,
					file.readText(),
				)
			})
		}
	}

}
