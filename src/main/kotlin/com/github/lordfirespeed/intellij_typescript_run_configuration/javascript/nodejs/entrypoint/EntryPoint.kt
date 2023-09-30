package com.github.lordfirespeed.intellij_typescript_run_configuration.javascript.nodejs.entrypoint

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.File

interface EntryPoint {
	fun resolve(context: Context): File?

	data class Context (
		val isEsm: Boolean,
		val isCjs: Boolean,
		val isNode: Boolean,
	)

	companion object {
		fun parseEntryWithFallback(reader: JsonReader): EntryPoint? {
			val nextToken = reader.peek()

			if (nextToken == JsonToken.BEGIN_ARRAY) {
				return FallbackEntryPoint.parse(reader)
			}

			return parseEntry(reader)
		}

		fun parseEntry(reader: JsonReader): EntryPoint? {
			val nextToken = reader.peek()

			if (nextToken == JsonToken.STRING) {
				return PathEntryPoint.parse(reader)
			}

			if (nextToken == JsonToken.BEGIN_OBJECT) {
				return ObjectEntryPoint.parse(reader)
			}

			return null;
		}
	}
}
