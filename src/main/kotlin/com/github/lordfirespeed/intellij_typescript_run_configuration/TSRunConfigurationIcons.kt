package com.github.lordfirespeed.intellij_typescript_run_configuration

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object TSRunConfigurationIcons {
	@JvmField
	val Run = AllIcons.Actions.Execute
	@JvmField
	val Debug = AllIcons.Actions.StartDebugger

	@JvmField
	val TypeScript = load("/com/github/lordfirespeed/intellij_typescript_run_configuration/icons/typescript@16.png")
	@JvmField
	val TypeScriptDebug = load("/com/github/lordfirespeed/intellij_typescript_run_configuration/icons/typescript-debug@16.png")

	private fun load(path: String): Icon
	{
		return IconLoader.getIcon(path, javaClass)
	}
}
