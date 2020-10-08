package ru.samokryl.jacoco.android

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import ru.samokryl.jacoco.android.extension.JacocoAndroidExtension
import ru.samokryl.jacoco.android.task.CreateFullReportTask

class JacocoPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		println("Apply jacoco plugin")

		val jacocoAndroidExtension = project.extensions.create("jacocoAndroid", JacocoAndroidExtension::class.java)

		project.plugins.apply("jacoco")
		configureJacocoExtension(project, jacocoAndroidExtension)

		val createFullReportTask = project.tasks.register("createFullReport", CreateFullReportTask::class.java)

	}

	private fun configureJacocoExtension(project: Project, jacocoAndroidExtension: JacocoAndroidExtension) {
		val jacocoExtension = project.extensions.getByType(JacocoPluginExtension::class.java)
		jacocoExtension.toolVersion = jacocoAndroidExtension.tool
	}
}