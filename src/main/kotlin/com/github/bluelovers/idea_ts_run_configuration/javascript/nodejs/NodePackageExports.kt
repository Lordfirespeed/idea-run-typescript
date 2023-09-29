package com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs

import com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs.entrypoint.EntryPoint
import com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs.entrypoint.ObjectEntryPoint
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import java.io.File

data class NodePackageExports(
	val packageName: String?,
	/** This is actually PersistentOrderedMap, but no interface for that is exposed. {@see https://github.com/Kotlin/kotlinx.collections.immutable/issues/106} */
	private val exportNameToEntryPointMapping: PersistentMap<String, EntryPoint>?
) {

	fun hasExportToEntryPointMapping(): Boolean = exportNameToEntryPointMapping != null

	fun resolveExportToEntryPoint(exportName: String, context: EntryPoint.Context): File? {
		if (this.exportNameToEntryPointMapping == null) return null

		val entryPoint = this.exportNameToEntryPointMapping[exportName] ?: return null
		return entryPoint.resolve(context)
	}

	class Builder {
		private var myPackageName: String? = null
		private var myExports: LinkedHashMap<String, EntryPoint>? = null

		fun setPackageName(packageName: String?) = apply { myPackageName = packageName }
		fun initializeExports() = apply { myExports = LinkedHashMap() }
		fun addExport(exportName: String, entryPoint: EntryPoint) = apply { myExports?.set(exportName, entryPoint) }

		private fun exportsAreObjectEntryPoint(): Boolean {
			return this.myExports?.none { entry -> entry.key.matches(EXPORT_NAME_PATTERN) } ?: false
		}

		private fun buildWhenExportsAreObjectEntryPoint() = NodePackageExports (
			myPackageName,
			persistentMapOf(
				"." to ObjectEntryPoint(myExports!!
					.filterKeys {
						ObjectEntryPoint.Environment.STRING_TO_ENVIRONMENT_MAPPING.containsKey(it)
					}
					.mapKeys {
						ObjectEntryPoint.Environment.STRING_TO_ENVIRONMENT_MAPPING[it.key]!!
					}
					.toPersistentMap()
				)
			)
		)

		private fun buildWhenExportsAreValid() = NodePackageExports(
			myPackageName,
			myExports?.filter { entry -> entry.key.matches(EXPORT_NAME_PATTERN) } ?.toPersistentMap()
		)

		fun build(): NodePackageExports {
			if (exportsAreObjectEntryPoint()) return buildWhenExportsAreObjectEntryPoint()
			return buildWhenExportsAreValid()
		}

		companion object {
			val EXPORT_NAME_PATTERN = Regex("^\\.(?:/.+)?")
		}
	}
}
