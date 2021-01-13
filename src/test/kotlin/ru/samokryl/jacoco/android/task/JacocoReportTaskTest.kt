package ru.samokryl.jacoco.android.task

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.samokryl.jacoco.android.extension.JacocoAndroidExtension
import java.io.File

class JacocoReportTaskTest {

	@get:Rule
	val temporaryFolder = TemporaryFolder()

	private val classpathList = listOf("tmp/kotlin-classes/")
	private val buildTypeName = "buildTypeName"
	private val testTaskName = "testTaskName"

	private val includedClasspath = listOf("test/package/**")
	private val excludedClasspath = listOf("firstExcluded", "secondExcluded")

	private lateinit var project: Project
	private lateinit var task: JacocoReportTask
	private lateinit var testBuildFile: File

	@Before
	fun setUp() {
		project = ProjectBuilder.builder()
			.withProjectDir(temporaryFolder.root)
			.build()

		task = project.tasks
			.register("jacocoReportTask", JacocoReportTask::class.java)
			.get()

		val jacocoExtension = project.extensions.create("jacocoAndroid", JacocoAndroidExtension::class.java)

		jacocoExtension.includedClasspath = includedClasspath
		jacocoExtension.excludedClasspath = excludedClasspath

		val kotlinClasses = temporaryFolder.newFolder("build/tmp/kotlin-classes/test/package")
		kotlinClasses.mkdirs()

		testBuildFile = File(kotlinClasses, "test.class")
		testBuildFile.writeText("Test")

		task.prepare(jacocoExtension, classpathList, buildTypeName, testTaskName)
	}

	@Test
	fun `prepare task EXPECT source files are main`() {
		val expected = project.files("src/main")
		assertEquals(expected.files, task.sourceDirectories.files)
	}

	@Test
	fun `prepare task EXPECT valid classDirectories`() {
		val expected = setOf(testBuildFile)

		assertEquals(expected, task.classDirectories.files)
	}

	@Test
	fun `prepare task EXPECT valid executionData`() {
		val expected = File(temporaryFolder.root, "build/jacoco/testTaskName.exec")

		assertEquals(setOf(expected), task.executionData.files)
	}
}