package com.github.bluelovers.idea_ts_run_configuration.execution

//import com.intellij.execution.runners.DefaultProgramRunner
//import com.intellij.javascript.debugger.execution.DebuggableProgramRunner
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.DefaultProgramRunner
import com.intellij.execution.runners.ProgramRunner
import com.github.bluelovers.idea_ts_run_configuration.lib.TsLog

class TsProgramRunner : DefaultProgramRunner()
{
	override fun getRunnerId() = javaClass.simpleName

	val LOG = TsLog(javaClass)

	override fun canRun(executorId: String, profile: RunProfile): Boolean
	{
		val bool = executorId == DefaultRunExecutor.EXECUTOR_ID && profile is TsRunConfiguration

		return bool
	}
}
