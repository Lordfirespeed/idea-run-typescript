package com.github.bluelovers.idea_ts_run_configuration

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object TsIcons
{
	@JvmField
	val Run = AllIcons.Actions.Execute
	@JvmField
	val Debug = AllIcons.Actions.StartDebugger

	@JvmField
	val TypeScript = load("/io/plugin/tsnode/icons/typescript@16.png")
	@JvmField
	val TypeScriptDebug = load("/io/plugin/tsnode/icons/typescript-debug@16.png")

	private fun load(path: String): Icon
	{
		return IconLoader.getIcon(path, javaClass)
	}
}
