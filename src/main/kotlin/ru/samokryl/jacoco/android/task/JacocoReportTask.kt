package ru.samokryl.jacoco.android.task

import org.gradle.api.file.ConfigurableFileTree
import org.gradle.testing.jacoco.tasks.JacocoReport
import ru.samokryl.jacoco.android.extension.JacocoAndroidExtension

open class JacocoReportTask : JacocoReport() {

	fun prepare(jacocoAndroidExtension: JacocoAndroidExtension, classpath: List<String>, buildTypeName: String, testTaskName: String) {
		logger.debug("prepare task for buildType: $buildTypeName, taskName: $testTaskName, classpath: $classpath")

		group = "jacoco"
		description = "Generate Jacoco coverage reports after running $buildTypeName tests."

		executionData.setFrom(project.files("${project.buildDir}/jacoco/$testTaskName.exec"))

		val sourceDirsPaths = mutableListOf("src/main")
		sourceDirectories.setFrom(project.files(sourceDirsPaths).files)

		classDirectories.setFrom(createConfiguredClassDirs(jacocoAndroidExtension, classpath))

		dependsOn(testTaskName)
	}

	private fun createConfiguredClassDirs(jacocoAndroidExtension: JacocoAndroidExtension, classpath: List<String>): ConfigurableFileTree {
		logger.debug("buildFolder: ${project.buildDir}")

		val configuredClassDirs = project.fileTree(project.buildDir)
		logger.debug("base dir: ${configuredClassDirs.files}")

		val includedClassPaths = jacocoAndroidExtension.includedClasspath
			.flatMap { configClassPath -> classpath.map { "$it$configClassPath" } }

		val excludedClassPaths = jacocoAndroidExtension.excludedClasspath
			.flatMap { configClassPath -> classpath.map { "$it$configClassPath" } }
			.toMutableList()

		excludedClassPaths.add("**/kotlin/*/caches-jvm")
		excludedClassPaths.add("**/tmp/expandedArchives/**")

		logger.debug("includedClassPaths: $includedClassPaths")
		logger.debug("excludedClassPaths: $excludedClassPaths")

		configuredClassDirs.include(includedClassPaths)
		configuredClassDirs.exclude(excludedClassPaths)

		logger.lifecycle("ConfiguredClassDirs: ${configuredClassDirs.files}")

		return configuredClassDirs
	}
}