package com.jave.homework321002

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class EditActivity : AppCompatActivity() {
    private lateinit var editTitle: EditText
    private lateinit var editDesc: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val predefinedVideo = run {
            val id = intent.extras?.getInt("id") ?: throw IllegalArgumentException("id required")
            (application as MyApplication).videos.find { it.id == id } ?: throw IllegalArgumentException("bad id")
        }
        title = predefinedVideo.name
        editTitle = findViewById(R.id.input_edit_title)
        editTitle.setText(predefinedVideo.name)
        editDesc = findViewById(R.id.input_edit_desc)
        editDesc.setText(predefinedVideo.description)

        findViewById<Button>(R.id.btn_save_edit).setOnClickListener {
            //保存视频信息

            File(filesDir, "${predefinedVideo.name}.txt").writeText(editDesc.text.toString())
            arrayOf(".txt", ".xml", ".mp4").forEach {
                File(filesDir, predefinedVideo.name + it).renameTo(File(filesDir, editTitle.text.toString() + it))
            }
            setResult(RESULT_OK)

            this.finish()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.add("导入弹幕")?.apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        filePickerActivity.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
            // application/xml不被文件应用认可。我不认可这种不认可，但它也不认可我对它不认可的不认可。
            type = "text/xml"
        })
        return true
    }

    private val filePickerActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != RESULT_OK) return@registerForActivityResult
        val inputUri = it.data?.data ?: return@registerForActivityResult
        contentResolver.openInputStream(inputUri)?.use { inputStream ->
            File(filesDir, "$title.xml").writeBytes(inputStream.readBytes())
        }
        Toast.makeText(this, "弹幕导入成功。", Toast.LENGTH_SHORT).show()
    }
}
