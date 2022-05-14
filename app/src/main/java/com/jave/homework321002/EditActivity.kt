package com.jave.homework321002

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
}
