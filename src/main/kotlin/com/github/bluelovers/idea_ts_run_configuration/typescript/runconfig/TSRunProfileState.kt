package com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig

import com.intellij.execution.ExecutionResult
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.javascript.debugger.CommandLineDebugConfigurator
import com.intellij.javascript.nodejs.debug.NodeCommandLineOwner
import com.intellij.javascript.nodejs.execution.NodeBaseRunProfileState
import org.jetbrains.concurrency.Promise

class TSRunProfileState(
	runConfiguration: TSRunConfiguration,
	environment: ExecutionEnvironment
): NodeBaseRunProfileState, NodeCommandLineOwner {
	override fun createExecutionResult(processHandler: ProcessHandler): ExecutionResult
	{
		TODO("Not yet implemented")
	}

	override fun startProcess(configurator: CommandLineDebugConfigurator?): ProcessHandler
	{
		TODO("Not yet implemented")
	}

	override fun execute(configurator: CommandLineDebugConfigurator?): Promise<ExecutionResult>
	{
		TODO("Not yet implemented")
	}
}
