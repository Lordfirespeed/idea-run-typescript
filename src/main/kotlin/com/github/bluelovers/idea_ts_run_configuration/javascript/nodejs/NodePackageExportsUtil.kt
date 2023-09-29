package com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs

import com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs.entrypoint.EntryPoint
import com.google.common.io.Files
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.containers.FixedHashMap
import com.intellij.webcore.util.JsonUtil
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

object NodePackageExportsUtil {
	private val LOG = Logger.getInstance(NodePackageExportsUtil::class.java)
	private val PACKAGE_EXPORTS_CACHE: MutableMap<String, TimestampedExports> = Collections.synchronizedMap(FixedHashMap(10))

	public fun getPackageExports(packageDirPath: String) = getPackageExportsByPackageJson(File(packageDirPath, "package.json"))

	private fun getPackageExportsByPackageJson(packageJson: File): NodePackageExports? {
		if (!packageJson.isFile) return null;

		val packageJsonPath = packageJson.absolutePath
		val cachedExports = PACKAGE_EXPORTS_CACHE[packageJsonPath]
		if (cachedExports != null && cachedExports.isUpToDate(packageJson)) return cachedExports.packageExports

		val packageExports = computePackageExportsByPackageJson(packageJson)
		val timestampedExports = TimestampedExports(packageJson, packageExports)
		PACKAGE_EXPORTS_CACHE[packageJsonPath] = timestampedExports

		return packageExports
	}

	private fun computePackageExportsByPackageJson(packageJson: File): NodePackageExports? {
		try {
			val reader = Files.newReader(packageJson, StandardCharsets.UTF_8)
			JsonReader(reader).use {
				return parseNodePackage(it)
			}
		} catch (error: IOException) {
			LOG.warn("Failed to parse " + packageJson.absolutePath, error)
		}
		return null
	}

	@Throws(IOException::class)
	private fun parseNodePackage(reader: JsonReader): NodePackageExports? {
		if (reader.peek() != JsonToken.BEGIN_OBJECT) {
			reader.skipValue()
			return null
		}

		reader.beginObject()
		val exportsBuilder = NodePackageExports.Builder()

		while (reader.hasNext()) {
			val key = reader.nextName()

			if (key == "name") {
				exportsBuilder.setPackageName(JsonUtil.nextStringOrSkip(reader))
				continue
			}

			if (key == "exports") {
				exportsBuilder.initializeExports()
				parseNodePackageExports(reader, exportsBuilder)
				continue
			}

			reader.skipValue()
		}

		reader.endObject()
		return exportsBuilder.build()
	}

	@Throws(IOException::class)
	private fun parseNodePackageExports(reader: JsonReader, builder: NodePackageExports.Builder) {
		if (reader.peek() == JsonToken.BEGIN_OBJECT) {
			return parseNodePackageExportToEntryMapping(reader, builder)
		}

		val export = EntryPoint.parseEntry(reader)

		if (export != null) {
			builder.addExport(".", export)
		}
	}

	@Throws(IOException::class, IOException::class)
	private fun parseNodePackageExportToEntryMapping(reader: JsonReader, builder: NodePackageExports.Builder) {
		if (reader.peek() != JsonToken.BEGIN_OBJECT) {
			reader.skipValue()
			return
		}

		reader.beginObject()

		while (reader.hasNext()) {
			val exportName = reader.nextName()
			val exportEntryPoint = EntryPoint.parseEntryWithFallback(reader) ?: continue
			builder.addExport(exportName, exportEntryPoint)
		}

		reader.endObject()
	}

	private class TimestampedExports(packageJson: File, val packageExports: NodePackageExports?) {
		private val myFileLastModified = packageJson.lastModified()
		private val myVfsModificationStamp = getVfsModificationStamp(packageJson)

		fun isUpToDate(packageJson: File): Boolean =
			myFileLastModified == packageJson.lastModified() && myVfsModificationStamp == getVfsModificationStamp(packageJson)

		companion object {
			private fun getVfsModificationStamp(packageJson: File): Long {
				if (ApplicationManager.getApplication().isUnitTestMode) {
					return -1L;
				}

				val vfsPackageJson = LocalFileSystem.getInstance().findFileByIoFile(packageJson)
				return vfsPackageJson?.modificationStamp ?: -1L
			}
		}
	}
}

fun NodePackage.getExports(): NodePackageExports? {
	if (!this.isValid(null, null)) return null
	return NodePackageExportsUtil.getPackageExports(this.systemDependentPath)
}

fun NodePackage.resolveExportEntryPoint(exportName: String, context: EntryPoint.Context): File? {
	val exports = this.getExports() ?: return null
	if (!exports.hasExportToEntryPointMapping()) return null
	val resolvedEntryPointRelativeFile = exports.resolveExportToEntryPoint(exportName, context) ?: return null
	return File(this.systemDependentPath).resolve(resolvedEntryPointRelativeFile).canonicalFile
}
