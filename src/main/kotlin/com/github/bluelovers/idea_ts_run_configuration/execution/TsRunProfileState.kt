package com.github.bluelovers.idea_ts_run_configuration.execution

import com.github.bluelovers.idea_ts_run_configuration.execution.TsUtil.isEmptyOrSpacesOrNull
import com.github.bluelovers.idea_ts_run_configuration.execution.TsUtil.tsnodePath
import com.github.bluelovers.idea_ts_run_configuration.execution.TsUtil.tsnodePathEsmLoader
import com.github.bluelovers.idea_ts_run_configuration.lib.TsLog
import com.github.bluelovers.idea_ts_run_configuration.lib.cmd.MyNodeCommandLineUtil
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilder
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.javascript.debugger.CommandLineDebugConfigurator
import com.intellij.javascript.nodejs.debug.NodeLocalDebuggableRunProfileState
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.search.ExecutionSearchScopes
import com.intellij.util.execution.ParametersListUtil
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise

open class TsRunProfileState(
	protected var project: Project,
	protected var runConfig: TsRunConfiguration,
	protected var executor: Executor,
	private final var myEnvironment: ExecutionEnvironment?) : NodeLocalDebuggableRunProfileState
{
	private var myConsoleBuilder: TextConsoleBuilder? = null
	private var debugConfigurator: CommandLineDebugConfigurator? = null
	val LOG = TsLog(javaClass)

	init {
		if (myEnvironment != null) {
			val project = myEnvironment!!.project
			val searchScope = ExecutionSearchScopes.executionScope(project, myEnvironment!!.runProfile)
			myConsoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project, searchScope)
		}
	}

	protected fun createCommandLine(): GeneralCommandLine
	{
		val runSettings = runConfig.runSettings
		val commandLine = MyNodeCommandLineUtil.createCommandLine(runConfig, project)

		MyNodeCommandLineUtil.configureDebugConfigurator(commandLine, this.debugConfigurator)

		MyNodeCommandLineUtil.configureWorkDirectory(commandLine, runSettings, project)

		MyNodeCommandLineUtil.configureEnvironment(commandLine, this.runConfig.envs)

		commandLine.exePath = runConfig.getInterpreterSystemDependentPath()

		val nodeOptionsList = ParametersListUtil.parse(runSettings.interpreterOptions.trim())
		commandLine.addParameters(nodeOptionsList)

		val enabledTsNodeEsmLoader = runSettings.enabledTsNodeEsmLoader

		val tsnode = tsnodePath(runConfig)

		if (enabledTsNodeEsmLoader)
		{
			if (isEmptyOrSpacesOrNull(tsnode))
			{
				commandLine.addParameter("--loader")
				commandLine.addParameter("ts-node/esm")
			}
			else
			{
				commandLine.addParameter("--loader")
				commandLine.addParameter(tsnodePathEsmLoader(runConfig))
			}
		}
		else
		{
			if (isEmptyOrSpacesOrNull(tsnode))
			{
				commandLine.addParameter("--require")
				commandLine.addParameter("ts-node/register")

				if (!StringUtil.isEmptyOrSpaces(runSettings.tsconfigFile))
				{
					commandLine.environment.putIfAbsent("TS_NODE_PROJECT", runSettings.tsconfigFile)
				}
			}
			else
			{
				commandLine.addParameter(tsnode)

				val typescriptOptionsList = ParametersListUtil.parse(runSettings.extraTypeScriptOptions.trim())
				commandLine.addParameters(typescriptOptionsList)

				if (!StringUtil.isEmptyOrSpaces(runSettings.tsconfigFile))
				{
					commandLine.addParameter("--project")
					commandLine.addParameter(runSettings.tsconfigFile)
				}
			}
		}

		if (!StringUtil.isEmptyOrSpaces(runSettings.scriptName))
		{
			commandLine.addParameter(runSettings.scriptName)

			if (!StringUtil.isEmptyOrSpaces(runSettings.programParameters))
			{
				val programParametersList = ParametersListUtil.parse(runSettings.programParameters.trim())

				commandLine.addParameters(programParametersList)
			}
		}

		return commandLine
	}

	@Throws(ExecutionException::class)
	protected fun createConsole(executor: Executor) = getConsoleBuilder()?.console

	protected fun startProcess(): OSProcessHandler
	{
		val commandLine = createCommandLine()

		MyNodeCommandLineUtil.configureCharset(commandLine)

		return MyNodeCommandLineUtil.createProcessHandler(commandLine, project, this.debugConfigurator)
	}

	protected fun createActions(console: ConsoleView?, processHandler: ProcessHandler?): Array<AnAction?>
	{
		return createActions(console, processHandler, null)
	}

	protected fun createActions(console: ConsoleView?, processHandler: ProcessHandler?, executor: Executor?): Array<AnAction?>
	{
		return AnAction.EMPTY_ARRAY
	}

	open fun getConsoleBuilder(): TextConsoleBuilder?
	{
		return myConsoleBuilder
	}

	open fun setConsoleBuilder(consoleBuilder: TextConsoleBuilder)
	{
		myConsoleBuilder = consoleBuilder
	}

	override fun execute(configurator: CommandLineDebugConfigurator?): Promise<ExecutionResult> {
		this.debugConfigurator = configurator

		val processHandler: ProcessHandler = startProcess()
		val console: ConsoleView? = createConsole(executor)
		console?.attachToProcess(processHandler)

		return resolvedPromise(DefaultExecutionResult(console, processHandler, *createActions(console, processHandler, executor)))
	}
}
