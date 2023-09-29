package com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs.entrypoint

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.intellij.webcore.util.JsonUtil
import java.io.File

data class PathEntryPoint(
	val path: String,
) : EntryPoint {
	override fun resolve(context: EntryPoint.Context): File {
		return File(path)
	}

	companion object {
		fun parse(reader: JsonReader): PathEntryPoint? {
			if (reader.peek() != JsonToken.STRING) {
				reader.skipValue()
				return null
			}

			val path = JsonUtil.nextStringOrSkip(reader) ?: return null
			return PathEntryPoint(path)
		}
	}
}

