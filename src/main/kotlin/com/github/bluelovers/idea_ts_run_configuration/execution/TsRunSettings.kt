package com.github.bluelovers.idea_ts_run_configuration.execution

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef

data class TsRunSettings(
	var interpreterRef: NodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef(),

	var enabledTsNodeEsmLoader: Boolean = false,

	var interpreterOptions: String = "",

	var workingDirectory: String = "",

	var envData: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT,

	var scriptName: String = "",
	var programParameters: String = "",

	var tsnodePackage: NodePackage? = null,

	var extraTypeScriptOptions: String = "",
	var tsconfigFile: String = ""
)
{
	var isPassParentEnvs: Boolean = envData.isPassParentEnvs
		get() = envData.isPassParentEnvs
}
