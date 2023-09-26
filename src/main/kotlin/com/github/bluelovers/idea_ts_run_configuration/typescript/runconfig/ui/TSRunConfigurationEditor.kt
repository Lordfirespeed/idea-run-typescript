package com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig.ui

import com.github.bluelovers.idea_ts_run_configuration.TSRunConfigurationBundle
import com.github.bluelovers.idea_ts_run_configuration.typescript.TSUtil
import com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig.TSRunConfiguration
import com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig.TSRunSettings
import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.execution.ui.CommonProgramParametersPanel
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.RawCommandLineEditor
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.SwingHelper
import com.intellij.webcore.ui.PathShortener
import javax.swing.JComponent
import javax.swing.JPanel

class TSRunConfigurationEditor(project: Project): SettingsEditor<TSRunConfiguration>() {
	private val interpreterField = NodeJsInterpreterField(project, false)
	private val interpreterOptionsField = RawCommandLineEditor().withMonospaced(false)
	private val workingDirectoryField = createWorkingDirField(project)
	private val typeScriptProjectFileField = createTypeScriptProjectFileField(project)
	private val executePackageField = NodePackageField(this.interpreterField, TSUtil.DESCRIPTOR) {
		val text = PathShortener.getAbsolutePath(this.workingDirectoryField.textField)
		return@NodePackageField if (text.isNotEmpty()) LocalFileSystem.getInstance().findFileByPath(text) else null
	}
	private val executorOptionsField = RawCommandLineEditor().withMonospaced(false)
	private val scriptPathField = createScriptPathField(project)
	private val applicationParametersField = RawCommandLineEditor().withMonospaced(false)
	private val envDataField = EnvironmentVariablesTextFieldWithBrowseButton()

	init {
		CommonProgramParametersPanel.addMacroSupport(interpreterOptionsField.editorField)
		CommonProgramParametersPanel.addMacroSupport(executorOptionsField.editorField)
		CommonProgramParametersPanel.addMacroSupport(applicationParametersField.editorField)
	}

	private val panel: JPanel = FormBuilder()
		.setAlignLabelOnRight(false)
		.addLabeledComponent(TSRunConfigurationBundle.message(NodeJsInterpreterField.getLabelTextForComponent()), interpreterField)
		.addLabeledComponent(TSRunConfigurationBundle.message("typescript.rc.nodeOptions.label"), interpreterOptionsField)
		.addLabeledComponent(TSRunConfigurationBundle.message("typescript.rc.workingDirectory.label"), workingDirectoryField)
		.addLabeledComponent(TSRunConfigurationBundle.message("typescript.rc.tsProjectPath.label"), typeScriptProjectFileField)
		.addLabeledComponent(TSRunConfigurationBundle.message("typescript.rc.tsExecutePackage.label"), executePackageField)
		.addLabeledComponent(TSRunConfigurationBundle.message("typescript.rc.tsExecuteOptions.label"), executorOptionsField)
		.addLabeledComponent(TSRunConfigurationBundle.message("typescript.rc.scriptPath.label"), scriptPathField)
		.addLabeledComponent(TSRunConfigurationBundle.message("typescript.rc.applicationParameters.label"), applicationParametersField)
		.addLabeledComponent(TSRunConfigurationBundle.message("typescript.rc.environmentVariables.label"), envDataField)
		.panel


	override fun resetEditorFrom(runConfiguration: TSRunConfiguration)
	{
		val runSettings: TSRunSettings = runConfiguration.runSettings
		interpreterField.interpreterRef = runSettings.interpreterRef
		interpreterOptionsField.text = runSettings.nodeOptions
		workingDirectoryField.text = runSettings.workingDirectory
		//typeScriptProjectFileField.text = todo
		executePackageField.selected = runConfiguration.getExecutePackage()
		//executorOptionsField.text = todo
		scriptPathField.text = runSettings.scriptPath
		//applicationParametersField.text = todo
		envDataField.data = runSettings.envData
	}

	@Throws(ConfigurationException::class)
	override fun applyEditorTo(runConfiguration: TSRunConfiguration)
	{
		val settingsBuilder = TSRunSettings.builder()
		settingsBuilder.apply {
			interpreterRef(interpreterField.interpreterRef)
			nodeOptions(interpreterOptionsField.text)
			workingDirectory(PathShortener.getAbsolutePath(workingDirectoryField.textField))
			//typescriptProjectFile()
			executePackage(executePackageField.selected)
			//executorOptions()
			scriptPath(PathShortener.getAbsolutePath(scriptPathField.textField))
			//applicationParameters()
			envData(envDataField.data)
		}

		runConfiguration.runSettings = settingsBuilder.build()
	}

	override fun createEditor(): JComponent
	{
		TODO("Not yet implemented")
	}

	companion object {
		fun createWorkingDirField(project: Project): TextFieldWithBrowseButton {
			val textFieldWithBrowseButton = TextFieldWithBrowseButton()
			SwingHelper.installFileCompletionAndBrowseDialog(project, textFieldWithBrowseButton, TSRunConfigurationBundle.message("typescript.rc.workingDirectory.browseDialogTitle", arrayOfNulls(0)), FileChooserDescriptorFactory.createSingleFolderDescriptor())
			return textFieldWithBrowseButton
		}

		fun createTypeScriptProjectFileField(project: Project): TextFieldWithBrowseButton {
			val textFieldWithBrowseButton = TextFieldWithBrowseButton()
			SwingHelper.installFileCompletionAndBrowseDialog(project, textFieldWithBrowseButton, TSRunConfigurationBundle.message("typescript.rc.tsProjectPath.browseDialogTitle", arrayOfNulls(0)), FileChooserDescriptorFactory.createSingleFolderDescriptor())
			return textFieldWithBrowseButton
		}

		fun createScriptPathField(project: Project): TextFieldWithBrowseButton {
			val textFieldWithBrowseButton = TextFieldWithBrowseButton()
			SwingHelper.installFileCompletionAndBrowseDialog(project, textFieldWithBrowseButton, TSRunConfigurationBundle.message("typescript.rc.scriptPath.browseDialogTitle", arrayOfNulls(0)), FileChooserDescriptorFactory.createSingleFolderDescriptor())
			return textFieldWithBrowseButton
		}
	}

}
