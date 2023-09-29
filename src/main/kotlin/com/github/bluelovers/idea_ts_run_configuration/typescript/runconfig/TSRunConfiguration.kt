package com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig

import com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig.ui.TSRunConfigurationEditor
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.javascript.nodejs.debug.NodeDebugRunConfiguration
import com.intellij.javascript.nodejs.execution.AbstractNodeTargetRunProfile
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import org.jdom.Element

@Suppress("ACCIDENTAL_OVERRIDE")
class TSRunConfiguration(
	project: Project,
	factory: ConfigurationFactory,
	name: String
): AbstractNodeTargetRunProfile(project, factory, name), NodeDebugRunConfiguration {
	var runSettings: TSRunSettings = TSRunSettings.builder().build()

	override val interpreter: NodeJsInterpreter? = this.runSettings.interpreterRef.resolve(this.project)

	@Throws(InvalidDataException::class)
	override fun readExternal(element: Element)	{
		super.readExternal(element)
		this.runSettings = TSRunSettings.readXml(element)
	}

	@Throws(WriteExternalException::class)
	override fun writeExternal(element: Element) {
		super.writeExternal(element)
		this.runSettings.writeXml(element)
	}

	@Throws(ExecutionException::class)
	override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState = TSRunProfileState(this, environment)

	override fun createConfigurationEditor(): TSRunConfigurationEditor = TSRunConfigurationEditor(this.project)

	@Throws(RuntimeConfigurationException::class)
	override fun checkConfiguration() {
		super.checkConfiguration()
	}
}
