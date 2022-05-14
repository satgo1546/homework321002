package com.jave.homework321002

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream


data class Danmaku(
	var time: Float,
	var type: Type,
	var fontSize: Float,
	var color: Int,
	var text: String,
) {
	enum class Type {
		SCROLLING, BOTTOM, TOP, Comment,
	}

	companion object {
		fun listFromXml(input: InputStream): ArrayList<Danmaku> {
			val parser = Xml.newPullParser().apply {
				setInput(input, "utf-8")
			}
			val ret = ArrayList<Danmaku>()
			while (true) {
				val eventType = parser.next()
				if (eventType == XmlPullParser.END_DOCUMENT) break
				if (eventType == XmlPullParser.START_TAG && parser.name == "d") {
					val params = parser.getAttributeValue(0).split(",")
					ret.add(
						Danmaku(
							params[0].toFloat(), when (params[1]) {
								"4" -> Type.BOTTOM
								"5" -> Type.TOP
								"1", "6" -> Type.SCROLLING
								"7" -> Type.TOP
								"8" -> Type.Comment  //时间点标记弹幕（歌词）
								else -> continue
							}, params[2].toFloat(), params[3].toInt() or 0xff000000.toInt(), parser.nextText()
						)
					)
				}
			}
			ret.sortBy { it.time }
			return ret
		}
	}
}
