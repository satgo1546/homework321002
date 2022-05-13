package com.jave.homework321002

// 最初本应用只能播放预定义视频，但现在可以播放其他来源的视频了，但为了兼容性（迫真）就不改名了
data class PredefinedVideo(
	val id: Int,
	val name: String,
	val description: String,
	val videoPath: String,
	val commentsPath: String,
)

val PredefinedVideos = mutableListOf(
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
