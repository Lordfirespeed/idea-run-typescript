package com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs.entrypoint

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import java.io.File

data class ObjectEntryPoint(
	/** This is actually PersistentOrderedMap, but no interface for that is exposed. {@see https://github.com/Kotlin/kotlinx.collections.immutable/issues/106} */
	val orderedEnvironmentToEntryPointMapping: PersistentMap<Environment, EntryPoint>
) : EntryPoint {
	override fun resolve(context: EntryPoint.Context): File?
	{
		orderedEnvironmentToEntryPointMapping.forEach {
			if (!it.key.matchesContext(context)) return@forEach
			return it.value.resolve(context) ?: return@forEach
		}
		return null
	}

	companion object {
		fun parse(reader: JsonReader): ObjectEntryPoint? {
			if (reader.peek() != JsonToken.BEGIN_OBJECT) {
				reader.skipValue()
				return null
			}

			reader.beginObject()

			val environmentToEntryPointMapping: LinkedHashMap<Environment, EntryPoint> = linkedMapOf()
			while (reader.hasNext()) {
				val exportEnvironment = Environment.STRING_TO_ENVIRONMENT_MAPPING[reader.nextName()] ?: continue
				val exportEntryPoint = EntryPoint.parseEntryWithFallback(reader) ?: continue
				environmentToEntryPointMapping[exportEnvironment] = exportEntryPoint
			}

			reader.endObject()

			return ObjectEntryPoint(environmentToEntryPointMapping.toPersistentMap())
		}
	}

	enum class Environment {
		REQUIRE  {
			override fun matchesContext(context: EntryPoint.Context) = context.isCjs
		},

		IMPORT {
			override fun matchesContext(context: EntryPoint.Context) = context.isEsm
		},

		NODE {
			override fun matchesContext(context: EntryPoint.Context) = context.isNode
		},

		DEFAULT {
			override fun matchesContext(context: EntryPoint.Context) = true
		};

		abstract fun matchesContext(context: EntryPoint.Context): Boolean

		companion object {
			val STRING_TO_ENVIRONMENT_MAPPING = persistentMapOf(
				"require" to REQUIRE,
				"import" to IMPORT,
				"node" to NODE,
				"default" to DEFAULT,
			)
		}
	}
}
