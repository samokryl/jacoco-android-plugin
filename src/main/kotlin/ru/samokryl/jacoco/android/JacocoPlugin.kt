package ru.samokryl.jacoco.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import ru.samokryl.jacoco.android.extension.JacocoAndroidExtension
import ru.samokryl.jacoco.android.task.CreateFullReportTask
import ru.samokryl.jacoco.android.task.JacocoReportTask
import ru.samokryl.jacoco.android.task.MergeReportsTask

class JacocoPlugin : Plugin<Project> {

	private companion object {

		private const val TMP_KOTLIN_FOLDER = "tmp/kotlin-classes"
	}

	private lateinit var logger: Logger

	override fun apply(project: Project) {
		logger = project.logger

		logger.lifecycle("Apply jacoco-android plugin")

		val jacocoAndroidExtension = project.extensions.create("jacocoAndroid", JacocoAndroidExtension::class.java)

		project.plugins.apply("jacoco")
		configureJacocoExtension(project, jacocoAndroidExtension)

		val mergeTask: MergeReportsTask = project.tasks.register("mergeFullReport", MergeReportsTask::class.java).get()

		val createFullReportTask = project.tasks.register("createFullReport", CreateFullReportTask::class.java) {
			it.executionData(project.files(mergeTask.destinationFile))
			it.dependsOn(mergeTask)
		}.get()

		project.afterEvaluate {
			it.subprojects.forEach { subProject ->
				subProject.afterEvaluate {
					val taskList = configureSubproject(subProject, jacocoAndroidExtension)

					addDependencies(subProject, taskList, mergeTask, createFullReportTask)
				}
			}
		}
	}

	private fun addDependencies(
		subProject: Project,
		taskList: List<JacocoReportTask>,
		mergeTask: MergeReportsTask,
		createFullReportTask: CreateFullReportTask
	) {
		taskList.forEach { task ->
			mergeTask.executionData = subProject.files(mergeTask.executionData, task.executionData)
			createFullReportTask.classDirectories
				.setFrom(subProject.files(createFullReportTask.classDirectories.files, task.classDirectories))
			createFullReportTask.sourceDirectories
				.setFrom(subProject.files(createFullReportTask.sourceDirectories.files, task.sourceDirectories))
		}
	}

	private fun configureJacocoExtension(project: Project, jacocoAndroidExtension: JacocoAndroidExtension) {
		logger.debug("Configure jacoco extension")

		val jacocoExtension = project.extensions.getByType(JacocoPluginExtension::class.java)
		jacocoExtension.toolVersion = jacocoAndroidExtension.tool

		logger.debug("Configure successful: $jacocoExtension")
	}

	private fun configureSubproject(
		subProject: Project,
		jacocoAndroidExtension: JacocoAndroidExtension
	): List<JacocoReportTask> {
		logger.debug("Configure subproject: '${subProject.name}'")

		if (subProject.name in jacocoAndroidExtension.ignoreModules) {
			logger.debug("The project '${subProject.name}' in an ignore modules list")
			return emptyList()
		}

		subProject.plugins.apply("jacoco")
		configureJacocoExtension(subProject, jacocoAndroidExtension)

		val extensions = subProject.extensions

		val variants = when {
			hasAndroidAppPlugin(subProject) -> extensions.getByType(AppExtension::class.java).applicationVariants
			hasAndroidLibPlugin(subProject) -> extensions.getByType(LibraryExtension::class.java).libraryVariants
			else                            -> null
		}

		logger.debug("Variants' size: '${variants?.size}'")

		val taskList = mutableListOf<JacocoReportTask?>()
		when {
			variants == null
				|| variants.isEmpty() -> taskList.add(addTaskWithoutVariant(subProject, jacocoAndroidExtension))
			else                      -> taskList.addAll(addTaskForSeveralVariants(subProject, jacocoAndroidExtension, variants))
		}

		return taskList.mapNotNull { it }
	}

	private fun addTaskWithoutVariant(
		subProject: Project,
		jacocoAndroidExtension: JacocoAndroidExtension,
	): JacocoReportTask? {
		val flavorName = ""
		val buildTypeName = ""

		return addTaskForBuildVariant(subProject, flavorName, buildTypeName, jacocoAndroidExtension)
	}

	private fun addTaskForSeveralVariants(
		subProject: Project,
		jacocoAndroidExtension: JacocoAndroidExtension,
		variants: DomainObjectSet<out BaseVariant>,
	): List<JacocoReportTask?> =
		variants.map { variant ->
			logger.debug("Variant '${variant.name}'")

			val flavorName = variant.flavorName ?: ""
			val buildTypeName = variant.buildType?.name ?: ""

			addTaskForBuildVariant(subProject, flavorName, buildTypeName, jacocoAndroidExtension)
		}.toList()

	private fun addTaskForBuildVariant(
		subProject: Project,
		flavorName: String,
		buildTypeName: String,
		jacocoAndroidExtension: JacocoAndroidExtension
	): JacocoReportTask? {
		if (flavorName.isNotEmpty() && jacocoAndroidExtension.testedFlavor != flavorName) {
			logger.debug("Return empty taskProvider because  flavorName: $flavorName")
			return null
		}

		if (buildTypeName.isNotEmpty() && jacocoAndroidExtension.testedBuildType != buildTypeName) {
			logger.debug("Return empty taskProvider because buildTypeName: $buildTypeName")
			return null
		}

		return addJacocoReportTask(subProject, jacocoAndroidExtension, flavorName, buildTypeName)
	}

	private fun addJacocoReportTask(
		subProject: Project,
		jacocoAndroidExtension: JacocoAndroidExtension,
		flavorName: String,
		buildTypeName: String
	): JacocoReportTask {
		logger.debug("Add jacocoReportTask to subProject: ${subProject.name}")

		val hasFlavor = flavorName.isNotEmpty()
		val hasBuildType = buildTypeName.isNotEmpty()

		val testTaskName = when {
			hasFlavor && hasBuildType  -> "test${flavorName.capitalize()}${buildTypeName.capitalize()}UnitTest"
			!hasFlavor && hasBuildType -> "test${buildTypeName.capitalize()}UnitTest"
			else                       -> "test"
		}

		logger.debug("testTaskName: $testTaskName")

		val classpath = when {
			hasFlavor && hasBuildType  -> listOf("**/${TMP_KOTLIN_FOLDER}/$flavorName${buildTypeName.capitalize()}/**")
			!hasFlavor && hasBuildType -> listOf("**/${TMP_KOTLIN_FOLDER}/$buildTypeName/")
			else                       -> listOf("**/classes/kotlin/main/**")
		}

		logger.debug("classpath: $classpath")

		val taskName = "jacocoReport${buildTypeName.capitalize()}"
		logger.debug("taskName: $taskName")

		return subProject.tasks.create(taskName, JacocoReportTask::class.java) {
			it.prepare(jacocoAndroidExtension, classpath, buildTypeName, testTaskName)
		}
	}

	private fun hasAndroidAppPlugin(project: Project): Boolean = project.plugins.hasPlugin("com.android.application")

	private fun hasAndroidLibPlugin(project: Project): Boolean = project.plugins.hasPlugin("com.android.library")
}