package com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs.entrypoint

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import java.io.File

data class FallbackEntryPoint(
	val orderedEntryPoints: PersistentList<EntryPoint>
) : EntryPoint {
	override fun resolve(context: EntryPoint.Context): File? {
		orderedEntryPoints.forEach {
			return it.resolve(context) ?: return@forEach
		}
		return null
	}

	companion object {
		fun parse(reader: JsonReader): FallbackEntryPoint? {
			if (reader.peek() != JsonToken.BEGIN_ARRAY) {
				reader.skipValue()
				return null
			}

			reader.beginArray()

			val entryPoints: MutableList<EntryPoint> = mutableListOf()
			while (reader.hasNext()) {
				val entryPoint = EntryPoint.parseEntry(reader) ?: continue
				entryPoints.add(entryPoint)
			}

			reader.endArray()

			return FallbackEntryPoint(entryPoints.toPersistentList())
		}
	}
}
