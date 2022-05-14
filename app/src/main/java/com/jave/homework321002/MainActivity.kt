package com.jave.homework321002

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
	private lateinit var listView: ListView
	private lateinit var inputTitle: EditText
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		title = "趣味音视频播放器 ←这什么土名字"

		listView = findViewById(R.id.listView)
		listView.adapter = VideosAdapter(this, (application as MyApplication).videos).also { it.filter.filter("") }
		listView.setOnItemClickListener { adapterView, view, i, l ->
			startActivity(Intent(this, PlayerActivity::class.java).apply {
				putExtra("id", (application as MyApplication).videos[i].id)
			})
		}
		listView.setOnItemLongClickListener { adapterView, view, i, l ->
			refreshListOnResult.launch(Intent(this, EditActivity::class.java).apply {
				putExtra("id", (application as MyApplication).videos[i].id)
			})
			true
		}

		findViewById<Button>(R.id.button4).setOnClickListener {
			startActivity(Intent(this, HelpActivity::class.java))
		}

		findViewById<Button>(R.id.btn_open).setOnClickListener {
			if (listView.adapter.isEmpty) return@setOnClickListener
			startActivity(Intent(this, PlayerActivity::class.java).apply {
				putExtra("id", (listView.adapter as VideosAdapter).currentList[0].id)
			})
		}

		inputTitle = findViewById(R.id.input_title)
		inputTitle.addTextChangedListener(object : TextWatcher {
			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
				(listView.adapter as VideosAdapter).filter.filter(s.toString())
			}

			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
			override fun afterTextChanged(s: Editable) = Unit
		})

		findViewById<Button>(R.id.button2).setOnClickListener {
			filePickerActivity.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
				// 如何指定音/视频？video/*选择视频，audio/*选择音频，没有办法指定并集。
				type = "*/*"
			})
		}

		findViewById<Button>(R.id.button3).setOnClickListener {
			ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)
		}
	}

	private val refreshListOnResult = registerForActivityResult(StartActivityForResult()) {
		(application as MyApplication).rescanVideos()
		(listView.adapter as VideosAdapter).filter.filter(inputTitle.text)
	}

	private val filePickerActivity = registerForActivityResult(StartActivityForResult()) {
		if (it.resultCode != RESULT_OK) return@registerForActivityResult
		val inputUri = it.data?.data ?: return@registerForActivityResult
		contentResolver.openInputStream(inputUri)?.use { inputStream ->
			val name = inputUri.lastPathSegment?.replace(Regex("[*?\":/\\\\|<>]"), "_") ?: SimpleDateFormat(
				"yyyy-MM-dd-HHmmss",
				Locale.US
			).format(Date())
			openFileOutput("$name.mp4", MODE_PRIVATE).use { outputStream ->
				inputStream.copyTo(outputStream)
			}
			addVideo(name)
		}
	}

	private var recordingName = ""

	private val videoCaptureActivity = registerForActivityResult(StartActivityForResult()) {
		if (it.resultCode != RESULT_OK) return@registerForActivityResult
		addVideo(recordingName)
	}

	private fun addVideo(name: String) {
		File(filesDir, "$name.txt").writeText("最新力作")
		File(filesDir, "$name.xml").writeText("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<i>\n</i>\n")
		(application as MyApplication).rescanVideos()
		// 通过重新过滤来刷新显示的列表。
		(listView.adapter as VideosAdapter).filter.filter(inputTitle.text)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
			Toast.makeText(this, "权限不足。", Toast.LENGTH_SHORT).show()
			return
		}
		recordingName = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US).format(Date())
		videoCaptureActivity.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
			putExtra(
				MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
					this@MainActivity, "$packageName.fileprovider", File(filesDir, "$recordingName.mp4")
				)
			)
		})
	}
}
