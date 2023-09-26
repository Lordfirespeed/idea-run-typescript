package com.github.bluelovers.idea_ts_run_configuration.typescript

import com.github.bluelovers.idea_ts_run_configuration.typescript.executor_packages.TSExecutorPackageDescriptor
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.PathUtil

object TSUtil {
	@Suppress("UnstableApiUsage") @NlsSafe final val ESM_TS_NODE_PACKAGE_NAME: String = "esm-ts-node"
	@Suppress("UnstableApiUsage") @NlsSafe final val TS_NODE_PACKAGE_NAME: String = "ts-node"
	@Suppress("UnstableApiUsage") @NlsSafe final val TSX_PACKAGE_NAME: String = "tsx"
	@Suppress("UnstableApiUsage") @NlsSafe final val ESNO_PACKAGE_NAME: String = "esno"

	final val DESCRIPTOR = TSExecutorPackageDescriptor()

	fun fileExtensionMatchesTypeScript(fileExtension: String?): Boolean {
		return fileExtension == "ts" || fileExtension == "tsx"
	}

	fun fileTypeMatchesTypeScript(fileType: FileType): Boolean {
		return fileType is TypeScriptFileType || fileType is TypeScriptJSXFileType
	}

	fun isTypeScriptScratch(psiFile: PsiFile): Boolean {
		if (!ScratchUtil.isScratch(psiFile.virtualFile)) return false
		val fileExtension = PathUtil.getFileExtension(psiFile.originalFile.toString())
		return fileExtensionMatchesTypeScript(fileExtension)
	}

	fun isTypeScriptScratch(virtualFile: VirtualFile?): Boolean {
		if (virtualFile == null) return false
		if (!ScratchUtil.isScratch(virtualFile)) return false
		val fileExtension = virtualFile.extension.toString()
		return fileExtensionMatchesTypeScript(fileExtension)
	}

	fun isTypeScript(psiFile: PsiFile): Boolean {
		if (psiFile.isDirectory) return false
		if (isTypeScriptScratch(psiFile)) return true
		return fileTypeMatchesTypeScript(psiFile.fileType)
	}

	fun isTypeScript(virtualFile: VirtualFile?): Boolean {
		if (virtualFile == null) return false
		if (virtualFile.isDirectory) return false
		if (isTypeScriptScratch(virtualFile)) return true
		return fileTypeMatchesTypeScript(virtualFile.fileType)
	}
}
