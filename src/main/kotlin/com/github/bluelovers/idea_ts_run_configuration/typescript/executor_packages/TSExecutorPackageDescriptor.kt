package com.github.bluelovers.idea_ts_run_configuration.typescript.executor_packages

import com.github.bluelovers.idea_ts_run_configuration.typescript.TSUtil
import com.intellij.javascript.nodejs.util.NodePackageDescriptor
import com.intellij.javascript.nodejs.util.NodePackageRef

class TSExecutorPackageDescriptor: NodePackageDescriptor(listOf(
	TSUtil.ESM_TS_NODE_PACKAGE_NAME,
	TSUtil.TS_NODE_PACKAGE_NAME,
	TSUtil.TSX_PACKAGE_NAME,
	TSUtil.ESNO_PACKAGE_NAME,
)) {
	override fun createPackageRef(text: String): NodePackageRef {
		if (this.packageNames.contains(text)) {
			return NodePackageRef.create(text);
		}

		return super.createPackageRef(text)
	}
}
