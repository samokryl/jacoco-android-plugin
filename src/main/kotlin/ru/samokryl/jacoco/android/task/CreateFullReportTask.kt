package ru.samokryl.jacoco.android.task

import org.gradle.testing.jacoco.tasks.JacocoReport
import ru.samokryl.jacoco.android.extension.JacocoAndroidExtension

open class CreateFullReportTask : JacocoReport() {

	init {
		group = "Jacoco"
	}

	fun setUp(jacocoAndroidExtension: JacocoAndroidExtension) {

	}
}