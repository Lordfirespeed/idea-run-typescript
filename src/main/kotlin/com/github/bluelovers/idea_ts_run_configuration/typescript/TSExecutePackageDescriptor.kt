package com.github.bluelovers.idea_ts_run_configuration.typescript

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageDescriptor
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class TSExecutePackageDescriptor(
	val isProjectFieldDescriptor: Boolean = false
): NodePackageDescriptor(listOf(
	TSExecuteUtil.ESM_TS_NODE_PACKAGE_NAME,
	TSExecuteUtil.TS_NODE_PACKAGE_NAME,
	TSExecuteUtil.TSX_PACKAGE_NAME,
	TSExecuteUtil.ESNO_PACKAGE_NAME,
)) {

	override fun createPackageRef(text: String): NodePackageRef {
		if (TSExecuteUtil.PROJECT_TS_EXECUTE_REFERENCE_NAME == text) {
			return NodePackageRef.create(text)
		}

		if (this.packageNames.contains(text)) {
			return NodePackageRef.create(text)
		}

		return super.createPackageRef(text)
	}

	override fun createPackage(path: String): NodePackage = TSExecuteNodePackage(LocalFilePath.stripRemoteProtocol(path))

	override fun dereferenceIfProjectRef(project: Project, ref: NodePackageRef): NodePackageRef {
		if (TSExecuteUtil.isProjectTsExecutePackageRef(ref)) {
			return TSExecuteManager.getInstance(project).tsExecutePackageRef
		}

		return super.dereferenceIfProjectRef(project, ref)
	}

	override fun listPackageRefs(project: Project, interpreter: NodeJsInterpreter?, contextFileOrDirectory: VirtualFile?): MutableList<NodePackageRef> {
		val packageRefs = super.listPackageRefs(project, interpreter, contextFileOrDirectory)

		if (!isProjectFieldDescriptor) {
			packageRefs.add(0, TSExecuteUtil.createProjectTsExecutePackageRef())
		}

		return packageRefs
	}
}
