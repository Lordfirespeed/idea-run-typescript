package com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig

import com.github.bluelovers.idea_ts_run_configuration.TSRunConfigurationBundle
import com.github.bluelovers.idea_ts_run_configuration.TSRunConfigurationIcons
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.NotNullLazyValue

class TSRunConfigurationType: ConfigurationTypeBase(
	"TypeScriptRunConfigurationType",
	TSRunConfigurationBundle.message("rc.type.name"),
		TSRunConfigurationBundle.message("rc.run.configuration.type.description"),
	NotNullLazyValue.lazy { TSRunConfigurationIcons.TypeScript }
), DumbAware {

	init {
		addFactory(TSConfigurationFactory(this))
	}

	companion object {
		fun getInstance() = ConfigurationTypeUtil.findConfigurationType(TSRunConfigurationType::class.java)
		fun getFactory() = getInstance().configurationFactories[0]
	}

}
