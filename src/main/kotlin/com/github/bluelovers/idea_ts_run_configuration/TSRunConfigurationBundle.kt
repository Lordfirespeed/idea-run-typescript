package com.github.bluelovers.idea_ts_run_configuration

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

const val BUNDLE = "messages.TSRunConfigurationBundle"

object TSRunConfigurationBundle : DynamicBundle(BUNDLE) {
	@Nls
	fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Array<Any?>): String = getMessage(key, *params)

	fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<@Nls String> =
		getLazyMessage(key, *params)

}
