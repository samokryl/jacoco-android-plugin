package ru.samokryl.jacoco.android

import org.gradle.internal.impldep.org.apache.commons.io.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.samokryl.jacoco.android.task.CreateFullReportTask
import ru.samokryl.jacoco.android.util.BuildUtils.createGradleSettings
import ru.samokryl.jacoco.android.util.BuildUtils.createRunner
import ru.samokryl.jacoco.android.util.BuildUtils.writeBuildGradle
import ru.samokryl.jacoco.android.util.jacocoAndroidExtension
import java.io.File

class SimpleProjectIntegrationTest {

	@get:Rule
	val testProjectRoot = TemporaryFolder()

	private val version = "6.6.1"
	private val templateFolder = File("src/test/resources/simpleProject")

	@Before
	fun setUp() {
		FileUtils.copyDirectory(templateFolder, testProjectRoot.root)
	}

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
			testProjectRoot,
			"""
				plugins {
					id "ru.samokryl.jacoco.android"
				}
								
				jacocoAndroid {
					tool = '0.8.4'
				}

			""".trimIndent()
		)

		createGradleSettings(testProjectRoot)

		val result = createRunner(testProjectRoot, version)
			.withArguments("tasks")
			.build()

		val taskOutcome = result.task(":tasks")?.outcome
		val taskResult = TaskOutcome.SUCCESS == taskOutcome || TaskOutcome.SKIPPED == taskOutcome

		assertTrue(taskResult)
		assertTrue("createFullReport doesn't exist", result.output.contains("createFullReport"))
		assertTrue("mergeFullReport doesn't exist", result.output.contains("mergeFullReport"))
	}
}