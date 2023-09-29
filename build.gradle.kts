import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishPluginTask
import org.jetbrains.intellij.tasks.VerifyPluginTask
import org.jetbrains.intellij.tasks.SignPluginTask
import org.jetbrains.intellij.tasks.RunIdeTask
import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

val pluginArchiveBaseName = properties("pluginArchiveBaseName").get()

plugins {
	idea
	java
	kotlin("jvm") version "1.9.10"
	alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
	alias(libs.plugins.changelog) // Gradle Changelog Plugin
}

allprojects {
	group = properties("pluginGroup").get()

	apply {
		plugin("idea")
		plugin("java")
		plugin("kotlin")
		plugin("org.jetbrains.intellij")
		plugin("org.jetbrains.changelog")
	}

	dependencies {
		//implementation(libs.annotations)
	}

	repositories {
		mavenCentral()
		maven("https://cache-redirector.jetbrains.com/repo.maven.apache.org/maven2")
		maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
	}

	configure<JavaPluginExtension> {
		sourceCompatibility = VERSION_17
		targetCompatibility = VERSION_17
	}

	intellij {
		version.set(properties("platformVersion"))
		type.set(properties("platformType"))

		downloadSources.set(true)
	}

	tasks {
		withType<KotlinCompile> {
			kotlinOptions {
				jvmTarget = VERSION_17.toString()
				languageVersion = "1.8"
				// see https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#kotlin-standard-library
				apiVersion = "1.6"
				freeCompilerArgs = listOf("-Xjvm-default=all")
			}
		}

		withType<ProcessResources> {
			duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		}

		withType<PatchPluginXmlTask> {
			sinceBuild.set(properties("pluginSinceBuild"))
			untilBuild.set(properties("pluginUntilBuild"))
		}

		// All these tasks don't make sense for non-root subprojects, plugin project enables them itself
		runIde { enabled = false }
		prepareSandbox { enabled = false }
		buildSearchableOptions { enabled = false }
	}

	// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
	changelog {
		groups.empty()
		repositoryUrl = properties("pluginRepositoryUrl")
	}

	sourceSets {
		main {
			resources.srcDirs("src/main/resources")
		}
	}

	kotlin {
		sourceSets {
			main {
				kotlin.srcDirs("src/main/kotlin")
			}
		}
	}

	dependencies {
		compileOnly(kotlin("stdlib"))
		compileOnly(kotlin("reflect"))

		testCompileOnly("junit:junit:4.11")
	}
}

val pluginProjects: List<Project>
	get() = rootProject.allprojects.filter { true }

project(":plugin") {
	version = properties("pluginVersion").get()

	intellij {
		pluginName.set(properties("pluginName"))
	}

	dependencies {
		implementation(project(":"))
	}

	// implementation from https://github.com/intellij-rust/intellij-rust/blob/db9bef0211a1698ebb09a50b5b4efa6b8d837a35/build.gradle.kts#
	// until https://github.com/JetBrains/gradle-intellij-plugin/issues/808 is implemented

	// Collects all jars produced by compilation of project modules and merges them into single one.
	// We need to put all plugin manifest files into single jar to make new plugin model work
	val mergePluginJarTask = task<Jar>("mergePluginJars") {
		duplicatesStrategy = DuplicatesStrategy.FAIL
		archiveBaseName.set(pluginArchiveBaseName)

		exclude("META-INF/MANIFEST.MF")
		exclude("**/classpath.index")

		val pluginLibDir by lazy {
			val sandboxTask = tasks.prepareSandbox.get()
			sandboxTask.destinationDir.resolve("${sandboxTask.pluginName.get()}/lib")
		}

		val pluginJars by lazy {
			pluginLibDir.listFiles().orEmpty().filter { it.isPluginJar() }
		}

		destinationDirectory.set(project.layout.dir(provider { pluginLibDir }))

		doFirst {
			for (file in pluginJars) {
				from(zipTree(file))
			}
		}

		doLast {
			delete(pluginJars)
		}
	}

	// Add plugin sources to the plugin ZIP.
	// gradle-intellij-plugin will use it as a plugin sources if the plugin is used as a dependency
	val createSourceJarTask = task<Jar>("createSourceJar") {
		for (prj in pluginProjects) {
			from(prj.kotlin.sourceSets.main.get().kotlin) {
				include("**/*.java")
				include("**/*.kt")
				duplicatesStrategy = DuplicatesStrategy.EXCLUDE
			}
		}

		destinationDirectory.set(layout.buildDirectory.dir("libs"))
		archiveBaseName.set(pluginArchiveBaseName)
		archiveClassifier.set("src")
	}

	tasks {
		buildPlugin {
			dependsOn(createSourceJarTask)
			from(createSourceJarTask) { into("lib/src") }
		}

		runIde {
			enabled = true
		}

		prepareSandbox {
			finalizedBy(mergePluginJarTask)
			enabled = true
		}

		buildSearchableOptions {
			dependsOn(mergePluginJarTask)
			enabled = true
		}

		withType<RunIdeTask> {
			mustRunAfter(mergePluginJarTask)

			// Default args for IDEA installation
			jvmArgs("-Xmx2G", "-XX:+UseG1GC", "-XX:SoftRefLRUPolicyMSPerMB=50")

			// Disable plugin auto reloading. See `com.intellij.ide.plugins.DynamicPluginVfsListener`
			jvmArgs("-Didea.auto.reload.plugins=false")

			// Don't show "Tip of the Day" at startup
			jvmArgs("-Dide.show.tips.on.startup.default.value=false")

			// uncomment if `unexpected exception ProcessCanceledException` prevents you from debugging a running IDE
			// jvmArgs("-Didea.ProcessCanceledException=disabled")

			// Uncomment to enable FUS testing mode
			// jvmArgs("-Dfus.internal.test.mode=true")

			// Uncomment to enable localization testing mode
			// jvmArgs("-Didea.l10n=true")
		}

		withType<PatchPluginXmlTask> {
			version.set(project.version.toString())

			// Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
			pluginDescription.set(providers.fileContents(rootProject.layout.projectDirectory.file("README.md")).asText.map {
				val start = "<!-- Plugin description -->"
				val end = "<!-- Plugin description end -->"

				with(it.lines()) {
					if (!containsAll(listOf(start, end))) {
						throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
					}
					subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
				}
			})

			val changelog = project.changelog // local variable for configuration cache compatibility
			// Get the latest available change notes from the changelog file
			changeNotes.set(properties("pluginVersion").map { pluginVersion ->
				with(changelog) {
					renderItem(
						(getOrNull(pluginVersion) ?: getUnreleased())
							.withHeader(false)
							.withEmptySections(false),
						Changelog.OutputType.HTML,
					)
				}
			})
		}

		withType<VerifyPluginTask> {
			mustRunAfter(mergePluginJarTask)
		}

		withType<SignPluginTask> {
			certificateChain.set(environment("CERTIFICATE_CHAIN"))
			privateKey.set(environment("PRIVATE_KEY"))
			password.set(environment("PRIVATE_KEY_PASSWORD"))
		}

		withType<PublishPluginTask> {
			dependsOn("patchChangelog")
			token.set(environment("PUBLISH_TOKEN"))
			// The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
			// Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
			// https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
			channels.set(properties("pluginVersion").map {
				listOf(it.split('-').getOrElse(1) { "default" }.split('.').first())
			})
		}
	}
}

project(":") {
	intellij {
		plugins.set(listOf("JavaScriptLanguage", "JavaScriptDebugger", "NodeJS"))
	}

	tasks {
		wrapper {
			gradleVersion = properties("gradleVersion").get()
		}
	}
}

fun File.isPluginJar(): Boolean {
	if (!isFile) return false
	if (extension != "jar") return false
	return zipTree(this).files.any {
		// TODO: make it more precise
		it.extension == "xml" && it.readText().trimStart().startsWith("<idea-plugin")
	}
}
