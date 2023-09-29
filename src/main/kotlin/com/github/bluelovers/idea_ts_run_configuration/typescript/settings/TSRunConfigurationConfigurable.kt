package com.github.bluelovers.idea_ts_run_configuration.typescript.settings

import com.github.bluelovers.idea_ts_run_configuration.TSRunConfigurationBundle
import com.github.bluelovers.idea_ts_run_configuration.typescript.TSExecuteManager
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class TSRunConfigurationConfigurable(
	private val project: Project
): SearchableConfigurable, Configurable.NoScroll {
	private var settingsView: TSRunConfigurationSettingsView? = null

	override fun createComponent(): JComponent {
		var view = this.settingsView
		if (view == null) {
			view = TSRunConfigurationSettingsView(project)
			this.settingsView = view
		}
		return view.form
	}

	override fun isModified(): Boolean {
		val view = this.settingsView ?: return false
		val prevTsExecutePackage: NodePackageRef = TSExecuteManager.getInstance(project).tsExecutePackageRef
		val nextTsExecutePackage: NodePackageRef = view.tsExecutePackageRef
		return prevTsExecutePackage != nextTsExecutePackage
	}

	override fun apply() {
		val view = this.settingsView ?: return

		TSExecuteManager.getInstance(project).tsExecutePackageRef = view.tsExecutePackageRef
	}

	override fun reset() {
		val view = this.settingsView ?: return;

		view.tsExecutePackageRef = TSExecuteManager.getInstance(project).tsExecutePackageRef
	}

	override fun getDisplayName(): String = TSRunConfigurationBundle.message("settings.typescript.execute.name")

	override fun getId(): String = ID

	companion object {
		const val ID = "settings.typescript.execute"
	}
}
