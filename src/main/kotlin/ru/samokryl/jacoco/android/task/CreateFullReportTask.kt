package ru.samokryl.jacoco.android.task

import org.gradle.testing.jacoco.tasks.JacocoReport

open class CreateFullReportTask : JacocoReport() {

	init {
		group = "Jacoco"
		description = "Create jacoco report for all modules"

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