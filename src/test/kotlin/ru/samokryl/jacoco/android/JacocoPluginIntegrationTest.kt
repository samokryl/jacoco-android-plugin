package ru.samokryl.jacoco.android

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.samokryl.jacoco.android.extension.JacocoAndroidExtension
import ru.samokryl.jacoco.android.task.CreateFullReportTask
import java.io.File

class JacocoPluginIntegrationTest {

	private val version = "6.6.1"

	@get:Rule
	val testProjectRoot = TemporaryFolder()

	@Test
	fun `apply project EXPECT apply successful`() {
		val project = ProjectBuilder.builder()
			.build()

		project.pluginManager.apply("ru.samokryl.jacoco.android")

		project.jacocoAndroidExtension()

		val task = project.tasks.getByName("createFullReport") as? CreateFullReportTask?
		assertNotNull(task)
	}

	@Test
	fun `run createFullReport for one module EXPECT report files exist`() {
		writeBuildGradle(
			"""
				plugins {
					id "ru.samokryl.jacoco.android"
				}
				
				jacocoAndroid {
					tool = '1.2.3'
				}

			""".trimIndent()
		)

		createGradleSettings()

		val result = createRunner()
			.withArguments("createFullReport")
			.build()

		result.output
			.contains("BUILD SUCCESS")
	}

	@Test
	fun `run createFullReport in multi-modules project EXPECT report files exist`() {
		TODO()
	}

	private fun writeBuildGradle(content: String) {
		createFile("build.gradle")
			.writeText(content)
	}

	private fun createGradleSettings() {
		createFile("settings.gradle")
	}

	private fun createFile(name: String): File =
		testProjectRoot.newFile(name)

	private fun createRunner(): GradleRunner =
		GradleRunner.create()
			.withProjectDir(testProjectRoot.root)
			.withPluginClasspath()
			.withGradleVersion(version)

	private fun Project.jacocoAndroidExtension(): JacocoAndroidExtension? =
		this.extensions.getByType(JacocoAndroidExtension::class.java)

}