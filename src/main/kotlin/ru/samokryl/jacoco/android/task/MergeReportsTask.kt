package ru.samokryl.jacoco.android.task

import org.gradle.testing.jacoco.tasks.JacocoMerge

open class MergeReportsTask : JacocoMerge() {

	private companion object {

		const val TASKS_GROUP = "jacoco"
	}

	init {
		group = TASKS_GROUP
		description = "Merge Jacoco coverage reports into single .exec file"
		executionData = project.files()
		destinationFile = project.file("${project.buildDir}/jacoco/mergedFullReport.exec")
	}
}