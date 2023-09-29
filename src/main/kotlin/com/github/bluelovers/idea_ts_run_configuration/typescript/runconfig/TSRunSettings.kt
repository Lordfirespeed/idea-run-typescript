package com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig

import com.github.bluelovers.idea_ts_run_configuration.typescript.TSExecuteUtil
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import org.jdom.Element

data class TSRunSettings(
	val interpreterRef: NodeJsInterpreterRef,
	val nodeOptions: String,
	val workingDirectory: String,
	val typescriptProjectFilePath: String,
	val executePackageRef: NodePackageRef,
	val executePackageOptions: String,

	val tsNodeUseEsmLoader: Boolean,

	val tsxLoaderType: TSXLoaderType,

	val scriptPath: String,
	val applicationParameters: String,
	val envData: EnvironmentVariablesData,
) {
	val isPassParentEnvs: Boolean = envData.isPassParentEnvs

	fun toBuilder() = Builder()
		.interpreterRef(interpreterRef)
		.nodeOptions(nodeOptions)
		.workingDirectory(workingDirectory)
		.typescriptProjectFilePath(typescriptProjectFilePath)
		.executePackageRef(executePackageRef)
		.executePackageOptions(executePackageOptions)
		.tsNodeUseEsmLoader(tsNodeUseEsmLoader)
		.tsxLoaderType(tsxLoaderType)
		.scriptPath(scriptPath)
		.applicationParameters(applicationParameters)
		.envData(envData)

	fun writeXml(element: Element): Unit {
		JDOMExternalizerUtil.writeCustomField(element, TAG_NODE_INTERPRETER, this.interpreterRef.referenceName)
		JDOMExternalizerUtil.writeCustomField(element, TAG_NODE_OPTIONS, this.nodeOptions)
		JDOMExternalizerUtil.writeCustomField(element, TAG_WORKING_DIR, FileUtil.toSystemIndependentName(this.workingDirectory))
		JDOMExternalizerUtil.writeCustomField(element, TAG_TS_PROJECT_FILE_PATH, FileUtil.toSystemIndependentName(this.typescriptProjectFilePath))

		if (!TSExecuteUtil.isProjectTsExecutePackageRef(this.executePackageRef)) {
			JDOMExternalizerUtil.writeCustomField(element, TAG_EXECUTOR_REF, this.executePackageRef.identifier)
		}
		JDOMExternalizerUtil.writeCustomField(element, TAG_EXECUTOR_OPTIONS, this.executePackageOptions)

		JDOMExternalizerUtil.writeCustomField(element, TAG_EXECUTOR_TS_NODE_USE_ESM_LOADER, this.tsNodeUseEsmLoader.toString())
		JDOMExternalizerUtil.writeCustomField(element, TAG_EXECUTOR_TSX_LOADER_TYPE, this.tsxLoaderType.toString())

		JDOMExternalizerUtil.writeCustomField(element, TAG_SCRIPT_PATH, FileUtil.toSystemIndependentName(this.scriptPath))
		JDOMExternalizerUtil.writeCustomField(element, TAG_APPLICATION_PARAMETERS, this.applicationParameters)
		this.envData.writeExternal(element)
	}

	companion object {
		const val TAG_NODE_INTERPRETER = "node-interpreter"
		const val TAG_NODE_OPTIONS = "node-options"
		const val TAG_WORKING_DIR = "working-directory"
		const val TAG_TS_PROJECT_FILE_PATH = "ts-project"
		const val TAG_EXECUTOR_REF = "ts-executor-package"
		const val TAG_EXECUTOR_OPTIONS = "ts-executor-options"

		const val TAG_EXECUTOR_TS_NODE_USE_ESM_LOADER = "ts-executor-ts-node-use-esm-loader"

		const val TAG_EXECUTOR_TSX_LOADER_TYPE = "ts-executor-tsx-loader-type"

		const val TAG_SCRIPT_PATH = "script"
		const val TAG_APPLICATION_PARAMETERS = "script-parameters"

		fun builder() = Builder()
		inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()

		fun readXml(element: Element): TSRunSettings {
			val thisBuilder = builder()

			thisBuilder.apply {
				interpreterRef(NodeJsInterpreterRef.create(JDOMExternalizerUtil.readCustomField(element, TAG_NODE_INTERPRETER)))
				nodeOptions(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, TAG_NODE_OPTIONS)))
				workingDirectory(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, TAG_WORKING_DIR)))
				typescriptProjectFilePath(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, TAG_TS_PROJECT_FILE_PATH)))
			}

			val executePackageReferenceName = JDOMExternalizerUtil.readCustomField(element, TAG_EXECUTOR_REF)
			thisBuilder.executePackageRef(
				if (executePackageReferenceName == null)
					TSExecuteUtil.createProjectTsExecutePackageRef()
				else
					TSExecuteUtil.DESCRIPTOR.createPackageRef(executePackageReferenceName)
			)
			thisBuilder.executePackageOptions(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, TAG_EXECUTOR_OPTIONS)))

			thisBuilder.tsNodeUseEsmLoader(JDOMExternalizerUtil.readCustomField(element, TAG_EXECUTOR_TS_NODE_USE_ESM_LOADER).toBoolean())
			val tsxLoaderType = TSXLoaderType.fromString(JDOMExternalizerUtil.readCustomField(element, TAG_EXECUTOR_TSX_LOADER_TYPE))
			if (tsxLoaderType != null) {
				thisBuilder.tsxLoaderType(tsxLoaderType)
			}

			thisBuilder.apply {
				scriptPath(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, TAG_SCRIPT_PATH)))
				applicationParameters(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, TAG_APPLICATION_PARAMETERS)))
				envData(EnvironmentVariablesData.readExternal(element))
			}

			return thisBuilder.build()
		}
	}

	class Builder {

		private var myInterpreterRef: NodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef()
		private var myNodeOptions: String = ""
		private var myWorkingDirectory: String = ""
		private var myTypescriptProjectFilePath: String = ""
		private var myExecutePackageRef: NodePackageRef = TSExecuteUtil.createProjectTsExecutePackageRef();
		private var myExecutePackageOptions: String = ""

		private var myTsNodeUseEsmLoader: Boolean = false

		private var myTsxLoaderType: TSXLoaderType = TSXLoaderType.COMBINED

		private var myScriptPath: String = ""
		private var myApplicationParameters: String = ""
		private var myEnvData: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT

		fun interpreterRef(interpreterRef: NodeJsInterpreterRef) = apply { this.myInterpreterRef = interpreterRef }
		fun nodeOptions(nodeOptions: String) = apply { this.myNodeOptions = nodeOptions }
		fun workingDirectory(workingDirectory: String) = apply { this.myWorkingDirectory = workingDirectory }
		fun typescriptProjectFilePath(typescriptProjectFilePath: String) = apply { this.myTypescriptProjectFilePath = typescriptProjectFilePath }
		fun scriptPath(scriptPath: String) = apply { this.myScriptPath = scriptPath }
		fun executePackageRef(executePackage: NodePackageRef) = apply { this.myExecutePackageRef = executePackage }
		fun executePackageOptions(executePackageOptions: String) = apply { this.myExecutePackageOptions = executePackageOptions }

		fun tsNodeUseEsmLoader(tsNodeUseEsmLoader: Boolean) = apply { this.myTsNodeUseEsmLoader = tsNodeUseEsmLoader }

		fun tsxLoaderType(tsxLoaderType: TSXLoaderType) = apply { this.myTsxLoaderType = tsxLoaderType }

		fun applicationParameters(applicationParameters: String) = apply { this.myApplicationParameters = applicationParameters }
		fun envData(envData: EnvironmentVariablesData) = apply { this.myEnvData = envData }

		fun build() = TSRunSettings(
			myInterpreterRef,
			myNodeOptions,
			FileUtil.toSystemDependentName(myWorkingDirectory),
			FileUtil.toSystemDependentName(myTypescriptProjectFilePath),
			myExecutePackageRef,
			myExecutePackageOptions,
			myTsNodeUseEsmLoader,
			myTsxLoaderType,
			FileUtil.toSystemDependentName(myScriptPath),
			myApplicationParameters,
			myEnvData,
		)

	}

	enum class TSXLoaderType {
		COMBINED {
			override val presentableName = "Combined"
		},
		ESM_ONLY {
			override val presentableName = "ESM-only"
		},
		CJS_ONLY {
			override val presentableName = "CJS-only"
		};

		abstract val presentableName: String
		override fun toString() = presentableName

		companion object {
			fun fromString(value: String?): TSXLoaderType? {
				return when (value) {
					COMBINED.presentableName -> COMBINED
					ESM_ONLY.presentableName -> ESM_ONLY
					CJS_ONLY.presentableName -> CJS_ONLY
					else -> null
				}
			}
		}
	}
}
