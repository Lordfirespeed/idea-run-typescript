package com.github.bluelovers.idea_ts_run_configuration.typescript

import com.github.bluelovers.idea_ts_run_configuration.TSRunConfigurationBundle
import com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs.entrypoint.EntryPoint
import com.github.bluelovers.idea_ts_run_configuration.javascript.nodejs.resolveExportEntryPoint
import com.github.bluelovers.idea_ts_run_configuration.typescript.runconfig.TSRunSettings
import com.intellij.execution.ExecutionException
import com.intellij.execution.target.TargetedCommandLineBuilder
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.text.SemVer
import java.io.File

class TSExecuteNodePackage(path: String): NodePackage(path) {
	@Throws(ExecutionException::class)
	fun configureTSExecutePackage(targetRun: NodeTargetRun, runSettings: TSRunSettings) {
		if (!this.isValid(targetRun.project, targetRun.interpreter)) {
			throw ExecutionException(TSRunConfigurationBundle.message("dialog.message.invalid.tsExecute.package"))
		}

		val packageName = this.name.trim()

		if (packageName == TSExecuteUtil.TS_NODE_PACKAGE_NAME) {
			return configureTSNode(targetRun, runSettings)
		}

		if (packageName == TSExecuteUtil.ESM_TS_NODE_PACKAGE_NAME) {
			return configureESMTSNode(targetRun, runSettings)
		}

		if (packageName == TSExecuteUtil.TSX_PACKAGE_NAME) {
			return configureTSX(targetRun, runSettings)
		}

		if (packageName == TSExecuteUtil.ESNO_PACKAGE_NAME) {
			return configureEsno(targetRun, runSettings)
		}

		throw ExecutionException(TSRunConfigurationBundle.message("dialog.message.unknown.tsExecute.package", arrayOf(packageName)))
	}

	@Throws(ExecutionException::class)
	private fun configureTSNode(targetRun: NodeTargetRun, runSettings: TSRunSettings) {
		val commandLine: TargetedCommandLineBuilder = targetRun.commandLineBuilder

		val tsProjectFile = getTsProjectFile(runSettings)
		if (tsProjectFile != null) commandLine.addEnvironmentVariable("TS_NODE_PROJECT", tsProjectFile.toString())

		if (runSettings.tsNodeUseEsmLoader) {
			configureTSNodeEsmLoader(commandLine)
		} else {
			configureTSNodeRegistrar(commandLine)
		}

		commandLine.addParameters(getPackageOptions(runSettings))
	}

	@Throws(ExecutionException::class)
	private fun configureTSNodeRegistrar(commandLine: TargetedCommandLineBuilder) {
		val tsNodeRegistrarPath = this.resolveExportEntryPoint("./register", EntryPoint.Context(isEsm = false, isCjs = true, isNode = true))
			?: throw ExecutionException("Couldn't read registrar export from ts-node package.")

		commandLine.addParameters("--require", tsNodeRegistrarPath.toString())
	}

	@Throws(ExecutionException::class)
	private fun configureTSNodeEsmLoader(commandLine: TargetedCommandLineBuilder) {
		val tsNodeEsmLoaderPath = this.resolveExportEntryPoint("./esm", EntryPoint.Context(isEsm = true, isCjs = true, isNode = true))
			?: throw ExecutionException("Couldn't read loader export from ts-node package.")

		commandLine.addParameters("--loader", tsNodeEsmLoaderPath.toURI().toString())
	}

	@Throws(ExecutionException::class)
	private fun configureESMTSNode(targetRun: NodeTargetRun, runSettings: TSRunSettings) {
		// Not really sure what this one should use?
		TODO("Not yet implemented.")
	}

	@Throws(ExecutionException::class)
	private fun configureTSX(targetRun: NodeTargetRun, runSettings: TSRunSettings) {
		val commandLine: TargetedCommandLineBuilder = targetRun.commandLineBuilder

		val tsProjectFile = getTsProjectFile(runSettings)
		if (tsProjectFile != null) commandLine.addEnvironmentVariable("ESBK_TSCONFIG_PATH", tsProjectFile.toString())

		configureTSXSelectedLoader(commandLine, runSettings.tsxLoaderType)

		commandLine.addParameters(getPackageOptions(runSettings))
	}

	@Throws(ExecutionException::class)
	private fun configureTSXSelectedLoader(commandLine: TargetedCommandLineBuilder, selected: TSRunSettings.TSXLoaderType) {
		when (selected) {
			TSRunSettings.TSXLoaderType.COMBINED -> configureTSXCombinedLoader(commandLine)
			TSRunSettings.TSXLoaderType.ESM_ONLY -> configureTSXEsmOnlyLoader(commandLine)
			TSRunSettings.TSXLoaderType.CJS_ONLY -> configureTSXCjsOnlyLoader(commandLine)
		}
	}

	@Throws(ExecutionException::class)
	private fun configureTSXCombinedLoader(commandLine: TargetedCommandLineBuilder) {
		val tsxLoaderPath = this.resolveExportEntryPoint(".", EntryPoint.Context(isEsm = true, isCjs = true, isNode = true))
			?: throw ExecutionException("Couldn't read loader export from tsx package.")

		commandLine.addParameters("--loader", tsxLoaderPath.toURI().toString())
	}

	@Throws(ExecutionException::class)
	private fun configureTSXEsmOnlyLoader(commandLine: TargetedCommandLineBuilder) {
		val tsxEsmLoaderPath = this.resolveExportEntryPoint("./esm", EntryPoint.Context(isEsm = true, isCjs = false, isNode = true))
			?: throw ExecutionException("Couldn't read loader export from tsx package.")

		commandLine.addParameters("--loader", tsxEsmLoaderPath.toURI().toString())
	}

	@Throws(ExecutionException::class)
	private fun configureTSXCjsOnlyLoader(commandLine: TargetedCommandLineBuilder) {
		val tsxCjsRegistrarPath = this.resolveExportEntryPoint("./cjs", EntryPoint.Context(isEsm = true, isCjs = true, isNode = true))
			?: throw ExecutionException("Couldn't read loader export from tsx package.")

		commandLine.addParameters("--require", tsxCjsRegistrarPath.toString())
	}

	@Throws(ExecutionException::class)
	private fun configureEsno(targetRun: NodeTargetRun, runSettings: TSRunSettings) {
		if (this.version!! < SemVer("0.15.0", 0, 15, 0)) {
			throw ExecutionException(TSRunConfigurationBundle.message("dialog.message.incompatible.tsExecute.package", arrayOf(this.name.trim())))
		}
		val tsxPackage = TSExecuteUtil.RESOLVER.resolve(NodePackageRef.create(TSExecuteUtil.TSX_PACKAGE_NAME)) ?: throw ExecutionException("TSX should be installed as it is a dependency of esno, but it couldn't be found.")

		configureTSExecutePackage(targetRun, runSettings)
	}

	private fun getTsProjectFile(runSettings: TSRunSettings): File? {
		if (StringUtil.isEmptyOrSpaces(runSettings.typescriptProjectFilePath)) return null
		return File(runSettings.typescriptProjectFilePath)
	}

	private fun getPackageOptions(runSettings: TSRunSettings): List<String> {
		return ProgramParametersConfigurator.expandMacrosAndParseParameters(runSettings.executePackageOptions)
	}

	companion object {
		@Throws(ExecutionException::class)
		fun configureTSExecutePackage(targetRun: NodeTargetRun, tsExecutePackage: NodePackage, runSettings: TSRunSettings) {
			if (tsExecutePackage !is TSExecuteNodePackage) return

			tsExecutePackage.configureTSExecutePackage(targetRun, runSettings)
		}
	}
}
