package ru.samokryl.jacoco.android.task

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.samokryl.jacoco.android.extension.JacocoAndroidExtension
import java.io.File

class CreateFullReportTaskTest {

	@get:Rule
	val temporaryFolder = TemporaryFolder()

	private val extension = JacocoAndroidExtension()

	private lateinit var project: Project

	private lateinit var task: CreateFullReportTask

	@Before
	fun setUp() {
		project = ProjectBuilder.builder()
			.withProjectDir(temporaryFolder.root)
			.build()

		task = project.tasks
			.register("createFullReport", CreateFullReportTask::class.java)
			.get()
	}

	@Test(expected = IllegalArgumentException::class)
	fun `empty jacocoAndroidExtension destinationFile EXPECT throw exception`() {
		task.configure(extension)
	}

	@Test
	fun `run task EXPECT execution data get from extension`() {
		val fileName = "build/test/file"

		extension.apply {
			destinationFile = fileName
		}

		val expected = File(temporaryFolder.root, fileName)

		task.configure(extension)
		val executionData = task.executionData.files

		assertTrue(executionData.contains(expected))
	}

	@Test
	fun `run task EXPECT reports block was configured`() {
		val fileName = "build/test/file"
		val reportDir = File(temporaryFolder.root, "build/reports/jacoco")

		val xmlDestination = File(reportDir, "jacoco.xml")
		val csvDestination = File(reportDir, "jacoco.csv")

		extension.apply {
			destinationFile = fileName
		}

		task.configure(extension)

		val xml = task.reports.xml
		val html = task.reports.html
		val csv = task.reports.csv

		assertTrue(xml.isEnabled)
		assertEquals(xmlDestination, xml.destination)

		assertTrue(html.isEnabled)
		assertEquals(reportDir, html.destination)

		assertTrue(csv.isEnabled)
		assertEquals(csvDestination, csv.destination)
	}
}