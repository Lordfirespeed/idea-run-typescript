package com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig

import com.github.bluelovers.idea_ts_run_configuration.TSRunConfigurationIcons
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import javax.swing.Icon

class TSConfigurationFactory(type: ConfigurationType): ConfigurationFactory(type) {
	override fun createTemplateConfiguration(project: Project): RunConfiguration =
		TSRunConfiguration(project, this, "TypeScript")

	override fun getName(): String = FACTORY_NAME

	override fun getIcon(): Icon = TSRunConfigurationIcons.TypeScript

	override fun getId(): String = RUN_CONFIGURATION_ID

	companion object {
		const val FACTORY_NAME = "TypeScript"
		const val RUN_CONFIGURATION_ID = "typescript.execute"
	}
}
