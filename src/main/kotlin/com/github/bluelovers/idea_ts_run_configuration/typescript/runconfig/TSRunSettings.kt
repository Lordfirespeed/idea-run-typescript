package com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig

import com.github.bluelovers.idea_ts_run_configuration.typescript.TSUtil
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import org.jdom.Element

data class TSRunSettings(
	val interpreterRef: NodeJsInterpreterRef,
	val nodeOptions: String,
	val workingDirectory: String,
	val envData: EnvironmentVariablesData,
	val scriptPath: String,
	val executePackage: NodePackage?
) {
	val isPassParentEnvs: Boolean = envData.isPassParentEnvs

	fun toBuilder() = Builder()
		.interpreterRef(interpreterRef)
		.nodeOptions(nodeOptions)
		.workingDirectory(workingDirectory)
		.envData(envData)
		.scriptPath(scriptPath)
		.executePackage(executePackage)

	fun writeXml(element: Element): Unit {
		JDOMExternalizerUtil.writeCustomField(element, "node-interpreter", this.interpreterRef.referenceName)
		JDOMExternalizerUtil.writeCustomField(element, "node-options", this.nodeOptions)
		JDOMExternalizerUtil.writeCustomField(element, "working-dir", FileUtil.toSystemIndependentName(this.workingDirectory))
		this.envData.writeExternal(element)
		JDOMExternalizerUtil.writeCustomField(element, "script-path", FileUtil.toSystemIndependentName(this.scriptPath))

		if (this.executePackage != null) {
			JDOMExternalizerUtil.writeCustomField(element, "ts-executor-package", this.executePackage.systemIndependentPath)
		}
	}

	companion object {
		fun builder() = Builder()
		inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()

		fun readXml(element: Element): TSRunSettings {
			val thisBuilder = builder()

			val interpreterRefName = JDOMExternalizerUtil.readCustomField(element, "node-interpreter")
			thisBuilder.interpreterRef(NodeJsInterpreterRef.create(interpreterRefName))
			thisBuilder.nodeOptions(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, "node-options")))
			thisBuilder.workingDirectory(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, "working-dir")))
			thisBuilder.envData(EnvironmentVariablesData.readExternal(element))
			thisBuilder.scriptPath(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, "script-path")))

			val packagePath = JDOMExternalizerUtil.readCustomField(element, "ts-executor-package")
			if (packagePath != null) {
				thisBuilder.executePackage(TSUtil.DESCRIPTOR.createPackage(packagePath))
			}

			return thisBuilder.build()
		}
	}

	class Builder {

		private var myInterpreterRef: NodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef()
		private var myNodeOptions: String = ""
		private var myWorkingDirectory: String = ""
		private var myEnvData: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
		private var myScriptPath: String = ""
		private var myExecutePackage: NodePackage? = null

		fun interpreterRef(interpreterRef: NodeJsInterpreterRef) = apply { this.myInterpreterRef = interpreterRef }
		fun nodeOptions(nodeOptions: String) = apply { this.myNodeOptions = nodeOptions }
		fun workingDirectory(workingDirectory: String) = apply { this.myWorkingDirectory = workingDirectory }
		fun envData(envData: EnvironmentVariablesData) = apply { this.myEnvData = envData }
		fun scriptPath(scriptPath: String) = apply { this.myScriptPath = scriptPath }
		fun executePackage(executePackage: NodePackage?) = apply { this.myExecutePackage = executePackage }

		fun build() = TSRunSettings(
			myInterpreterRef,
			myNodeOptions,
			FileUtil.toSystemDependentName(myWorkingDirectory),
			myEnvData,
			FileUtil.toSystemDependentName(myScriptPath),
			myExecutePackage
		)

	}

}
