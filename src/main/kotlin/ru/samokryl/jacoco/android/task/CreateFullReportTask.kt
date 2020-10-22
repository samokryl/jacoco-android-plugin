package ru.samokryl.jacoco.android.task

import org.gradle.api.tasks.Input
import org.gradle.testing.jacoco.tasks.JacocoReport
import ru.samokryl.jacoco.android.extension.JacocoAndroidExtension

open class CreateFullReportTask : JacocoReport() {

	init {
		group = "Jacoco"
		description = "Create jacoco report for all modules"
	}

	@Input
	lateinit var jacocoAndroidExtension: JacocoAndroidExtension

	fun configure(jacocoAndroidExtension: JacocoAndroidExtension) {
		val destinationFile = jacocoAndroidExtension.destinationFile
			?: throw IllegalArgumentException("JacocoAndroidExtension.destinationFile hasn't set")

		executionData(project.files(destinationFile))

		reports.apply {
			xml.apply {
				isEnabled = true
				destination = project.file("${project.buildDir}/reports/jacoco/jacoco.xml")
			}
			csv.apply {
				isEnabled = true
				destination = project.file("${project.buildDir}/reports/jacoco/jacoco.csv")
			}
			html.apply {
				isEnabled = true
				destination = project.file("${project.buildDir}/reports/jacoco")
			}
		}

		classDirectories.setFrom(project.files("${project.buildDir}/jacoco"))
	}
}