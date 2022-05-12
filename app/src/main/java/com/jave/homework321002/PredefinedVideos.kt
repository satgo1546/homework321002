package com.jave.homework321002

data class PredefinedVideo(
	val id: Int,
	val name: String,
	val discription: String,
	val videoResourceId: Int,
	val commentsResourceId: Int,
)

val PredefinedVideos = listOf(
	// https://developer.android.com/guide/topics/media/media-formats
	PredefinedVideo(1, "有屏幕的地方就有……", "",R.raw.bad_apple, R.raw.bad_apple_comments), // av706
	PredefinedVideo(
		2, "还可以往里面添加其他视频", "", R.raw.never_gonna_give_you_up, R.raw.never_gonna_give_you_up_comments
	), // av677985054
)
