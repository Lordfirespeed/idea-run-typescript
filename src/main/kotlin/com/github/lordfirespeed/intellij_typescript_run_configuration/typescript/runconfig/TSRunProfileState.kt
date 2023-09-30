package com.github.lordfirespeed.intellij_typescript_run_configuration.typescript.runconfig

import com.github.lordfirespeed.intellij_typescript_run_configuration.typescript.TSExecuteUtil
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.javascript.debugger.CommandLineDebugConfigurator
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.NodeConsoleAdditionalFilter
import com.intellij.javascript.nodejs.NodeStackTraceFilter
import com.intellij.javascript.nodejs.debug.NodeCommandLineOwner
import com.intellij.javascript.nodejs.execution.NodeBaseRunProfileState
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.lang.javascript.buildTools.TypeScriptErrorConsoleFilter
import com.intellij.util.ThreeState
import com.intellij.util.execution.ParametersListUtil
import java.io.File

class TSRunProfileState(
	private val runConfiguration: TSRunConfiguration,
	private val environment: ExecutionEnvironment
): NodeBaseRunProfileState, NodeCommandLineOwner {
	private val runSettings = runConfiguration.runSettings
	private val nodeInterpreter = runSettings.interpreterRef.resolveNotNull(environment.project)

	override fun createExecutionResult(processHandler: ProcessHandler): ExecutionResult {
		ProcessTerminatedListener.attach(processHandler)
		val console: ConsoleView = createConsole(processHandler, File(runSettings.workingDirectory))
		console.attachToProcess(processHandler)
		foldCommandLine(console, processHandler)

		return DefaultExecutionResult(console, processHandler)
	}

	override fun startProcess(configurator: CommandLineDebugConfigurator?): ProcessHandler {
		val targetRun = NodeTargetRun(
			nodeInterpreter,
			environment.project,
			configurator,
			NodeTargetRun.createOptions(
				ThreeState.UNSURE,
				listOf(),
				true,
				null,
				environment.runProfile as TSRunConfiguration
			)
		)
		configureNodeTargetRun(targetRun, runSettings)
		return targetRun.startProcess()
	}

	private fun createConsole(processHandler: ProcessHandler, cwd: File?): ConsoleView {
		val console: ConsoleView = NodeCommandLineUtil.createConsole(processHandler, environment.project, false)

		console.addMessageFilter(NodeStackTraceFilter(environment.project, cwd))
		console.addMessageFilter(NodeConsoleAdditionalFilter(environment.project, cwd))
		console.addMessageFilter(TypeScriptErrorConsoleFilter(environment.project, cwd))

		return console
	}

	companion object {
		@Throws(ExecutionException::class)
		fun configureNodeTargetRun(targetRun: NodeTargetRun, runSettings: TSRunSettings) {
			targetRun.configureEnvironment(runSettings.envData)

			val expandedNodeOptions = ProgramParametersConfigurator.expandMacrosAndParseParameters(runSettings.nodeOptions)
			targetRun.addNodeOptionsWithExpandedMacros(true, ParametersListUtil.join(expandedNodeOptions))

			TSExecuteUtil.configureTSExecuteCommand(targetRun, runSettings)
		}
	}
}
