package com.github.lordfirespeed.intellij_typescript_run_configuration.typescript.runconfig.ui

import com.github.lordfirespeed.intellij_typescript_run_configuration.TSRunConfigurationBundle
import com.github.lordfirespeed.intellij_typescript_run_configuration.typescript.TSExecuteUtil
import com.github.lordfirespeed.intellij_typescript_run_configuration.typescript.runconfig.TSRunConfiguration
import com.github.lordfirespeed.intellij_typescript_run_configuration.typescript.runconfig.TSRunSettings
import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.execution.ui.CommonProgramParametersPanel
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.util.DefaultNodePackageRefResolver
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.SwingHelper
import com.intellij.webcore.ui.PathShortener
import javax.swing.JComponent

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
	private val executePackageOptionsField = RawCommandLineEditor().withMonospaced(false)

	// ts-node specific
	private val selectedExecutePackageIsTsNode = selectedExecutePackageIdentifier.transform { it == TSExecuteUtil.TS_NODE_PACKAGE_NAME }
	private val tsNodeUseEsmLoaderCheckbox = JBCheckBox(TSRunConfigurationBundle.message("rc.tsExecutePackage.ts-node.useEsmLoader.detail"))

	// tsx specific
	private val selectedExecutePackageIsTSX = selectedExecutePackageIdentifier.transform { it == TSExecuteUtil.TSX_PACKAGE_NAME || it == TSExecuteUtil.ESNO_PACKAGE_NAME }
	private val tsxLoaderTypeComboBox = ComboBox(arrayOf(
		TSRunSettings.TSXLoaderType.COMBINED,
		TSRunSettings.TSXLoaderType.ESM_ONLY,
		TSRunSettings.TSXLoaderType.CJS_ONLY,
	))

	private val scriptPathField = createScriptPathField(project)
	private val applicationParametersField = RawCommandLineEditor().withMonospaced(false)
	private val envDataField = EnvironmentVariablesTextFieldWithBrowseButton()

	init {
		executePackageField.addSelectionListener { selectedExecutePackageIdentifier.set(it.name) }

		CommonProgramParametersPanel.addMacroSupport(interpreterOptionsField.editorField)
		CommonProgramParametersPanel.addMacroSupport(executePackageOptionsField.editorField)
		CommonProgramParametersPanel.addMacroSupport(applicationParametersField.editorField)
	}

	override fun resetEditorFrom(runConfiguration: TSRunConfiguration)
	{
		val runSettings: TSRunSettings = runConfiguration.runSettings
		interpreterField.interpreterRef = runSettings.interpreterRef
		interpreterOptionsField.text = runSettings.nodeOptions
		workingDirectoryField.text = runSettings.workingDirectory
		typescriptProjectFileField.text = runSettings.typescriptProjectFilePath
		executePackageField.selectedRef = runSettings.executePackageRef
		executePackageOptionsField.text = runSettings.executePackageOptions

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

	override fun createEditor(): JComponent {
		return panel {
			row(NodeJsInterpreterField.getLabelTextForComponent()) {
				cell(interpreterField)
					.align(AlignX.FILL)
			}.layout(RowLayout.PARENT_GRID)

			row(TSRunConfigurationBundle.message("rc.nodeOptions.label")) {
				cell(workingDirectoryField)
					.align(AlignX.FILL)
			}.layout(RowLayout.PARENT_GRID)

			row(TSRunConfigurationBundle.message("rc.tsProjectPath.label")) {
				cell(typescriptProjectFileField)
					.align(AlignX.FILL)
			}.layout(RowLayout.PARENT_GRID)

			row(TSRunConfigurationBundle.message("rc.tsExecutePackage.label")) {
				cell(executePackageField)
					.align(AlignX.FILL)
			}.layout(RowLayout.PARENT_GRID)

			row(TSRunConfigurationBundle.message("rc.tsExecuteOptions.label")) {
				cell(executePackageOptionsField)
					.align(AlignX.FILL)
			}.layout(RowLayout.PARENT_GRID)

			collapsibleGroup("ts-node Specific Options") {
				row(TSRunConfigurationBundle.message("rc.tsExecutePackage.ts-node.useEsmLoader.label")) {
					cell(tsNodeUseEsmLoaderCheckbox)
						.align(AlignX.FILL)
				}.layout(RowLayout.PARENT_GRID)
			}.visibleIf(selectedExecutePackageIsTsNode)

			collapsibleGroup("tsx Specific Options") {
				row("Loader type:") {
					cell(tsxLoaderTypeComboBox)
						.align(AlignX.FILL)
				}.layout(RowLayout.PARENT_GRID)
			}.visibleIf(selectedExecutePackageIsTSX)

			row(TSRunConfigurationBundle.message("rc.scriptPath.label")) {
				cell(scriptPathField)
					.align(AlignX.FILL)
			}.layout(RowLayout.PARENT_GRID)

			row(TSRunConfigurationBundle.message("rc.applicationParameters.label")) {
				cell(applicationParametersField)
					.align(AlignX.FILL)
			}.layout(RowLayout.PARENT_GRID)

			row(TSRunConfigurationBundle.message("rc.environmentVariables.label")) {
				cell(envDataField)
					.align(AlignX.FILL)
			}.layout(RowLayout.PARENT_GRID)
		}
	}

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
