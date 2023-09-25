package com.github.bluelovers.idea_ts_run_configuration.execution

//import com.intellij.execution.runners.DefaultProgramRunner
//import com.intellij.javascript.debugger.execution.DebuggableProgramRunner
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.DefaultProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.javascript.debugger.execution.DebuggableProgramRunner
import com.github.bluelovers.idea_ts_run_configuration.lib.TsLog

/**
 * @fixme https://github.com/bluelovers/idea-run-typescript/pull/14#issuecomment-678684547
 */
class TsDebugProgramRunner : DebuggableProgramRunner()
{
	override fun getRunnerId() = javaClass.simpleName

	val LOG = TsLog(javaClass)

	override fun canRun(executorId: String, profile: RunProfile): Boolean
	{
		val bool = executorId === "Debug" && profile is TsRunConfiguration

		return bool
	}
}
