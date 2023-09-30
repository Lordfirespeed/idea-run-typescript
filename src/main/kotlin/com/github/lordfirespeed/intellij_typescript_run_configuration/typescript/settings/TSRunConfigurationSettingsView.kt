package com.github.lordfirespeed.intellij_typescript_run_configuration.typescript.settings

import com.github.lordfirespeed.intellij_typescript_run_configuration.TSRunConfigurationBundle
import com.github.lordfirespeed.intellij_typescript_run_configuration.typescript.TSExecutePackageDescriptor
import com.intellij.javascript.nodejs.util.DefaultNodePackageRefResolver
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.openapi.project.Project
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class TSRunConfigurationSettingsView(project: Project) {
	private val tsExecutePackageField: NodePackageField = createTsExecutePackageField(project)

	val form: JPanel = FormBuilder.createFormBuilder()
		.setAlignLabelOnRight(false)
		.addLabeledComponent(TSRunConfigurationBundle.message("settings.typescript.tsExecutePackage.label"), tsExecutePackageField)
		.addComponentFillVertically(JPanel(), 0)
		.panel

	var tsExecutePackageRef: NodePackageRef
		get() = tsExecutePackageField.selectedRef
		set(value) { tsExecutePackageField.selectedRef = value }


	companion object {
		fun createTsExecutePackageField(project: Project): NodePackageField {
			val interpreterSupplier = { TypeScriptLanguageServiceUtil.getNodeInterpreter(project) }
			val descriptor = TSExecutePackageDescriptor(true)
			val resolver = DefaultNodePackageRefResolver(project, descriptor, interpreterSupplier)
			return NodePackageField(project, descriptor, interpreterSupplier, resolver)
		}
	}
}
