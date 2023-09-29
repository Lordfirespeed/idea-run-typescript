package com.github.bluelovers.idea_ts_run_configuration.typescript

import com.intellij.ide.util.PropertiesComponent
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.twelvemonkeys.lang.StringUtil


@Service(Service.Level.PROJECT)
class TSExecuteManager(private val project: Project) {
	@Volatile
	var tsExecutePackageRef: NodePackageRef = getDefaultPackageRef()
		set(value) {
			field = value
			var identifier: String? = value.identifier
			if (StringUtil.isEmpty(identifier)) {
				identifier = null
			}
			PropertiesComponent.getInstance(project).setValue(TS_EXECUTE_PACKAGE_REF_PROPERTY_KEY, identifier)
		}

	private fun getDefaultPackageRef(): NodePackageRef {
		val propertiesComponent = PropertiesComponent.getInstance(project)
		val identifier = propertiesComponent.getValue(TS_EXECUTE_PACKAGE_REF_PROPERTY_KEY)

		if (!StringUtil.isEmpty(identifier)) return TSExecuteUtil.DESCRIPTOR.createPackageRef(identifier!!)
		if (project.isDefault) return NodePackageRef.create(NodePackage(""))

		val packageRef = detectPackageRef()
		propertiesComponent.setValue(TS_EXECUTE_PACKAGE_REF_PROPERTY_KEY, identifier)
		return packageRef
	}

	private fun detectPackageRef(): NodePackageRef {
		val detectedPackageRef = detectInstalledTsExecutePackage()
		if (detectedPackageRef != null) return detectedPackageRef

		return NodePackageRef.create(TSExecuteUtil.TS_NODE_PACKAGE_NAME)
	}

	private fun detectInstalledTsExecutePackage(): NodePackageRef? =
		TSExecuteUtil.DESCRIPTOR.listPackageRefs(project, null, null).firstOrNull()

	companion object {
		private val TS_EXECUTE_PACKAGE_REF_PROPERTY_KEY = "typescript_execute_package_path"
		fun getInstance(project: Project): TSExecuteManager = project.getService(TSExecuteManager::class.java)
	}
}
