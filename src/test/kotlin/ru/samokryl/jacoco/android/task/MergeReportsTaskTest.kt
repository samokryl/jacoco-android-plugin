package ru.samokryl.jacoco.android.task

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MergeReportsTaskTest {

	@get:Rule
	val temporaryFolder = TemporaryFolder()

	private lateinit var project: Project
	private lateinit var mergeReportsTask: MergeReportsTask

	@Before
	fun setUp() {
		project = ProjectBuilder.builder()
			.withProjectDir(temporaryFolder.root)
			.build()

		mergeReportsTask = project.tasks
			.register("mergeReportTask", MergeReportsTask::class.java)
			.get()
	}

	@Test
	fun `create task EXPECT valid destination file`() {
		val expected = File(temporaryFolder.root, "build/jacoco/mergedFullReport.exec")

		assertEquals(expected, mergeReportsTask.destinationFile)
	}
}