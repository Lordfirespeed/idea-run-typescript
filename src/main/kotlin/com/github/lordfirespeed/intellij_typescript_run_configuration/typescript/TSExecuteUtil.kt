package com.github.lordfirespeed.intellij_typescript_run_configuration.typescript

import com.github.lordfirespeed.intellij_typescript_run_configuration.TSRunConfigurationBundle
import com.github.lordfirespeed.intellij_typescript_run_configuration.typescript.runconfig.TSRunSettings
import com.intellij.execution.ExecutionException
import com.intellij.execution.target.TargetedCommandLineBuilder
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.util.DefaultNodePackageRefResolver
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.PathUtil

object TSExecuteUtil {
	@Suppress("UnstableApiUsage") @NlsSafe const val ESM_TS_NODE_PACKAGE_NAME: String = "esm-ts-node"
	@Suppress("UnstableApiUsage") @NlsSafe const val TS_NODE_PACKAGE_NAME: String = "ts-node"
	@Suppress("UnstableApiUsage") @NlsSafe const val TSX_PACKAGE_NAME: String = "tsx"
	@Suppress("UnstableApiUsage") @NlsSafe const val ESNO_PACKAGE_NAME: String = "esno"

	const val PROJECT_TS_EXECUTE_REFERENCE_NAME = "Project"

	val DESCRIPTOR = TSExecutePackageDescriptor()
	val RESOLVER = DefaultNodePackageRefResolver(null, DESCRIPTOR, null)

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

	fun createProjectTsExecutePackageRef(): NodePackageRef = NodePackageRef.create(PROJECT_TS_EXECUTE_REFERENCE_NAME)

	fun isProjectTsExecutePackageRef(packageRef: NodePackageRef): Boolean = PROJECT_TS_EXECUTE_REFERENCE_NAME == packageRef.referenceName

	@Throws(ExecutionException::class)
	fun configureTSExecuteCommand(targetRun: NodeTargetRun, runSettings: TSRunSettings) {
		val tsExecutePackage: NodePackage? = resolveRef(runSettings.executePackageRef, targetRun.project, targetRun.interpreter)
		if (tsExecutePackage == null) {
			if (isProjectTsExecutePackageRef(runSettings.executePackageRef)) {
				//todo: this should throw a subclass of ExecutionException which can re-run the configuration once a tsExecute package has been set. See NpmUtil#configureNpmCommand
				throw ExecutionException(TSRunConfigurationBundle.message("dialog.message.cannot.resolve.tsExecute.package", arrayOf(TSExecuteManager.getInstance(targetRun.project).tsExecutePackageRef.identifier)))
			}

			throw ExecutionException(TSRunConfigurationBundle.message("dialog.message.cannot.resolve.tsExecute.package", arrayOf(TSExecuteManager.getInstance(targetRun.project).tsExecutePackageRef.identifier)))
		}

		val commandLine: TargetedCommandLineBuilder = targetRun.commandLineBuilder

		if (!StringUtil.isEmptyOrSpaces(runSettings.workingDirectory)) {
			commandLine.setWorkingDirectory(targetRun.path(runSettings.workingDirectory))
		}

		TSExecuteNodePackage.configureTSExecutePackage(targetRun, tsExecutePackage, runSettings)

		commandLine.addParameter(runSettings.scriptPath)
		val expandedApplicationParameters = ProgramParametersConfigurator.expandMacrosAndParseParameters(runSettings.applicationParameters)

		commandLine.addParameters(expandedApplicationParameters)
		NodeCommandLineUtil.prependNodeDirToPATH(targetRun)
	}

	fun resolveRef(ref: NodePackageRef, project: Project, interpreter: NodeJsInterpreter): NodePackage? {
		return RESOLVER.resolve(DESCRIPTOR.dereferenceIfProjectRef(project, ref), interpreter)
	}
}
