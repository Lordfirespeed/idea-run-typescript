package com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig.ui

import com.github.bluelovers.idea_ts_run_configuration.TSRunConfigurationBundle
import com.github.bluelovers.idea_ts_run_configuration.typescript.TSExecuteUtil
import com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig.TSRunConfiguration
import com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig.TSRunSettings
import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.execution.ui.CommonProgramParametersPanel
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.util.DefaultNodePackageRefResolver
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.observable.util.bindVisible
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.impl.CollapsibleTitledSeparatorImpl
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.SwingHelper
import com.intellij.webcore.ui.PathShortener
import javax.swing.JComponent
import javax.swing.JPanel

class TSRunConfigurationEditor(project: Project): SettingsEditor<TSRunConfiguration>() {
	private val interpreterField = NodeJsInterpreterField(project, false)
	private val interpreterOptionsField = RawCommandLineEditor().withMonospaced(false)
	private val workingDirectoryField = createWorkingDirField(project)
	private val typescriptProjectFileField = createTypeScriptProjectFileField(project)

	private val executePackageField = NodePackageField(
		this.interpreterField,
		TSExecuteUtil.DESCRIPTOR,
		null,
		DefaultNodePackageRefResolver(
			project,
			TSExecuteUtil.DESCRIPTOR,
			interpreterField::getInterpreter
		)
	)
	private val selectedExecutePackageIdentifier = AtomicLazyProperty { executePackageField.selected.name }
	private val executorOptionsField = RawCommandLineEditor().withMonospaced(false)

	// ts-node specific
	private val selectedExecutePackageIsTsNode = selectedExecutePackageIdentifier.transform { it == TSExecuteUtil.TS_NODE_PACKAGE_NAME }
	private val tsNodeUseEsmLoaderCheckbox = JBCheckBox(TSRunConfigurationBundle.message("rc.tsExecutePackage.ts-node.useEsmLoader.detail"))
	private val tsNodeSpecificPanelToggle = CollapsibleTitledSeparatorImpl("ts-node Specific Options")
	private val tsNodeSpecificPanelContent = FormBuilder()
		.setFormLeftIndent(16)
		.setAlignLabelOnRight(false)
		.addLabeledComponent(TSRunConfigurationBundle.message("rc.tsExecutePackage.ts-node.useEsmLoader.label"), tsNodeUseEsmLoaderCheckbox)
		.panel
		.bindVisible(tsNodeSpecificPanelToggle.expandedProperty)
	private val tsNodeSpecificPanel = FormBuilder()
		.setAlignLabelOnRight(false)
		.addComponent(tsNodeSpecificPanelToggle)
		.addComponent(tsNodeSpecificPanelContent)
		.panel
		.bindVisible(selectedExecutePackageIsTsNode)

	// tsx specific
	private val selectedExecutePackageIsTSX = selectedExecutePackageIdentifier.transform { it == TSExecuteUtil.TSX_PACKAGE_NAME || it == TSExecuteUtil.ESNO_PACKAGE_NAME }
	private val tsxLoaderTypeComboBox = ComboBox(arrayOf(
		TSRunSettings.TSXLoaderType.COMBINED,
		TSRunSettings.TSXLoaderType.ESM_ONLY,
		TSRunSettings.TSXLoaderType.CJS_ONLY,
	))
	private val tsxSpecificPanelToggle = CollapsibleTitledSeparatorImpl("tsx Specific Options")
	private val tsxSpecificPanelContent = FormBuilder()
		.setFormLeftIndent(16)
		.setAlignLabelOnRight(false)
		.addLabeledComponent("Loader type:", tsxLoaderTypeComboBox)
		.panel
		.bindVisible(tsxSpecificPanelToggle.expandedProperty)
	private val tsxSpecificPanel = FormBuilder()
		.setAlignLabelOnRight(false)
		.addComponent(tsxSpecificPanelToggle)
		.addComponent(tsxSpecificPanelContent)
		.panel
		.bindVisible(selectedExecutePackageIsTSX)

	private val scriptPathField = createScriptPathField(project)
	private val applicationParametersField = RawCommandLineEditor().withMonospaced(false)
	private val envDataField = EnvironmentVariablesTextFieldWithBrowseButton()

	init {
		executePackageField.addSelectionListener { print(it.name); selectedExecutePackageIdentifier.set(it.name) }

		CommonProgramParametersPanel.addMacroSupport(interpreterOptionsField.editorField)
		CommonProgramParametersPanel.addMacroSupport(executorOptionsField.editorField)
		CommonProgramParametersPanel.addMacroSupport(applicationParametersField.editorField)
	}

	private val panel: JPanel = FormBuilder()
		.setAlignLabelOnRight(false)
		.addLabeledComponent(NodeJsInterpreterField.getLabelTextForComponent(), interpreterField)
		.addLabeledComponent(TSRunConfigurationBundle.message("rc.nodeOptions.label"), interpreterOptionsField)
		.addLabeledComponent(TSRunConfigurationBundle.message("rc.workingDirectory.label"), workingDirectoryField)
		.addLabeledComponent(TSRunConfigurationBundle.message("rc.tsProjectPath.label"), typescriptProjectFileField)
		.addLabeledComponent(TSRunConfigurationBundle.message("rc.tsExecutePackage.label"), executePackageField)
		.addLabeledComponent(TSRunConfigurationBundle.message("rc.tsExecuteOptions.label"), executorOptionsField)
		.addComponent(tsNodeSpecificPanel)
		.addComponent(tsxSpecificPanel)
		.addLabeledComponent(TSRunConfigurationBundle.message("rc.scriptPath.label"), scriptPathField)
		.addLabeledComponent(TSRunConfigurationBundle.message("rc.applicationParameters.label"), applicationParametersField)
		.addLabeledComponent(TSRunConfigurationBundle.message("rc.environmentVariables.label"), envDataField)
		.panel

	override fun resetEditorFrom(runConfiguration: TSRunConfiguration)
	{
		val runSettings: TSRunSettings = runConfiguration.runSettings
		interpreterField.interpreterRef = runSettings.interpreterRef
		interpreterOptionsField.text = runSettings.nodeOptions
		workingDirectoryField.text = runSettings.workingDirectory
		typescriptProjectFileField.text = runSettings.typescriptProjectFilePath
		executePackageField.selectedRef = runSettings.executePackageRef
		executorOptionsField.text = runSettings.executePackageOptions

		tsNodeUseEsmLoaderCheckbox.isSelected = runSettings.tsNodeUseEsmLoader

		tsxLoaderTypeComboBox.selectedItem = runSettings.tsxLoaderType

		scriptPathField.text = runSettings.scriptPath
		applicationParametersField.text = runSettings.applicationParameters
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
			typescriptProjectFilePath(PathShortener.getAbsolutePath(typescriptProjectFileField.textField))
			executePackageRef(executePackageField.selectedRef)
			executePackageOptions(applicationParametersField.text)

			tsNodeUseEsmLoader(tsNodeUseEsmLoaderCheckbox.isSelected)

			tsxLoaderType(tsxLoaderTypeComboBox.selectedItem as TSRunSettings.TSXLoaderType)

			scriptPath(PathShortener.getAbsolutePath(scriptPathField.textField))
			applicationParameters(applicationParametersField.text)
			envData(envDataField.data)
		}

		runConfiguration.runSettings = settingsBuilder.build()
	}

	override fun createEditor(): JComponent = this.panel

	companion object {
		fun createWorkingDirField(project: Project): TextFieldWithBrowseButton {
			val textFieldWithBrowseButton = TextFieldWithBrowseButton()
			SwingHelper.installFileCompletionAndBrowseDialog(project, textFieldWithBrowseButton, TSRunConfigurationBundle.message("rc.workingDirectory.browseDialogTitle", arrayOfNulls(0)), FileChooserDescriptorFactory.createSingleFolderDescriptor())
			return textFieldWithBrowseButton
		}

		fun createTypeScriptProjectFileField(project: Project): TextFieldWithBrowseButton {
			val textFieldWithBrowseButton = TextFieldWithBrowseButton()
			SwingHelper.installFileCompletionAndBrowseDialog(project, textFieldWithBrowseButton, TSRunConfigurationBundle.message("rc.tsProjectPath.browseDialogTitle", arrayOfNulls(0)), FileChooserDescriptorFactory.createSingleFolderDescriptor())
			return textFieldWithBrowseButton
		}

		fun createScriptPathField(project: Project): TextFieldWithBrowseButton {
			val textFieldWithBrowseButton = TextFieldWithBrowseButton()
			SwingHelper.installFileCompletionAndBrowseDialog(project, textFieldWithBrowseButton, TSRunConfigurationBundle.message("rc.scriptPath.browseDialogTitle", arrayOfNulls(0)), FileChooserDescriptorFactory.createSingleFolderDescriptor())
			return textFieldWithBrowseButton
		}
	}

}
