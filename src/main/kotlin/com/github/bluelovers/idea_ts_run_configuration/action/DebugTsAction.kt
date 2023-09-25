package com.github.bluelovers.idea_ts_run_configuration.action

import com.github.bluelovers.idea_ts_run_configuration.TsIcons

/**
 * @todo make `TsDebugProgramRunner` can be fully replace `DebugTsAction`
 */
class DebugTsAction : TsAction(TsIcons.TypeScriptDebug)
{
	override val _debug = true
}
