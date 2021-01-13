package ru.samokryl.jacoco.android.extension

open class JacocoAndroidExtension {

	var excludedClasspath: List<String> = emptyList()
	var includedClasspath: List<String> = listOf("*")
	var testedBuildType: String = ""
	var testedFlavor: String = ""
	var ignoreModules: List<String> = emptyList()

	var tool: String = ""

}