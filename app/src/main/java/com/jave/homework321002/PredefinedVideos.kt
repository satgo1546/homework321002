package com.jave.homework321002

data class PredefinedVideo(
	val id: Int,
	val name: String,
	val resourceId: Int,
)

val PredefinedVideos = arrayOf(
	// https://developer.android.com/guide/topics/media/media-formats
	PredefinedVideo(1, "有屏幕的地方就有……", R.raw.bad_apple), // av706
	PredefinedVideo(2, "还可以往里面添加其他视频", R.raw.never_gonna_give_you_up), // av677985054
)
