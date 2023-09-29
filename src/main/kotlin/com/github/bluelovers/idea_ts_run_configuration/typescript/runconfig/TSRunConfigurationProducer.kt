package com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig

import com.github.bluelovers.idea_ts_run_configuration.typescript.TSExecuteUtil
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement

class TSRunConfigurationProducer: LazyRunConfigurationProducer<TSRunConfiguration>() {
	override fun getConfigurationFactory(): ConfigurationFactory = TSRunConfigurationType.getFactory()

	override fun setupConfigurationFromContext(configuration: TSRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>): Boolean {
		val location = context.location ?: return false

		val psiElement = location.psiElement
		if (!psiElement.isValid) {
			return false
		}

		val psiFile = psiElement.containingFile

		if (!TSExecuteUtil.isTypeScript(psiFile)) {
			return false
		}

		val virtualFile = location.virtualFile ?: return false

		sourceElement.set(psiFile)

		configuration.name = virtualFile.presentableName

		val runSettingsBuilder = configuration.runSettings.toBuilder()
		runSettingsBuilder.scriptPath(virtualFile.path)

		if (ScratchUtil.isScratch(psiFile.virtualFile)) {
			// make ScratchFile run in project path
		}
		else if (virtualFile.parent != null) {
			runSettingsBuilder.workingDirectory(virtualFile.parent.path)
		}

		configuration.runSettings = runSettingsBuilder.build()

		return true
	}

	override fun isConfigurationFromContext(configuration: TSRunConfiguration, context: ConfigurationContext): Boolean {
		val location = context.location ?: return false

		val virtualFile = location.virtualFile

		return virtualFile != null && FileUtil.pathsEqual(virtualFile.path, configuration.runSettings.scriptPath)
	}

}
