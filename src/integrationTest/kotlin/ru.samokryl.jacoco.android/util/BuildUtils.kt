package ru.samokryl.jacoco.android.util

import org.gradle.api.Project
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import ru.samokryl.jacoco.android.extension.JacocoAndroidExtension
import java.io.File

object BuildUtils {

	fun writeBuildGradle(temporaryFolder: TemporaryFolder, content: String) {
		createFile(temporaryFolder, "build.gradle")
			.writeText(content)
	}

	fun createGradleSettings(temporaryFolder: TemporaryFolder) {
		createFile(temporaryFolder, "settings.gradle")
	}

	private fun createFile(temporaryFolder: TemporaryFolder, name: String): File =
		temporaryFolder.newFile(name)

	fun createRunner(temporaryFolder: TemporaryFolder, version: String): GradleRunner =
		GradleRunner.create()
			.withProjectDir(temporaryFolder.root)
			.withPluginClasspath()
			.withGradleVersion(version)
}

fun Project.jacocoAndroidExtension(): JacocoAndroidExtension? =
	this.extensions.getByType(JacocoAndroidExtension::class.java)