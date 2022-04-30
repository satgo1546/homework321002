package com.jave.homework321002

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		title = "趣味音视频播放器 ←这什么土名字"
		val videos = arrayOf(
			// https://developer.android.com/guide/topics/media/media-formats
			Pair("有屏幕的地方就有……", R.raw.bad_apple), // av706
			Pair("还可以往里面添加其他视频", R.raw.never_gonna_give_you_up), // av677985054
		)
		val listView = findViewById<ListView>(R.id.listView)
		listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, videos.map { it.first })
		listView.setOnItemClickListener { adapterView, view, i, l ->
			startActivity(Intent(this, PlayerActivity::class.java).apply {
				putExtra("id", videos[i].second)
			})
		}
	}
}
