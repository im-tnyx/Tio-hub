package com.tnyx.shared.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class SharedKmpBoundaryTest {
    @Test
    fun sharedMainSourceDoesNotUsePlatformSpecificTokens() {
        val sourceRoot = resolveSharedMainSourceRoot()
        val forbiddenTokens = listOf(
            "androidx.",
            "android.",
            "java.time",
            "System.currentTimeMillis",
            "Context",
            "NavController",
            "@Immutable",
            "@Stable",
            "Hilt",
            "Room"
        )
        val violations = mutableListOf<String>()

        Files.walk(sourceRoot).use { paths ->
            paths
                .filter { path -> Files.isRegularFile(path) && path.fileName.toString().endsWith(".kt") }
                .forEach { path ->
                    val relativePath = sourceRoot.relativize(path).toString().replace('\\', '/')
                    Files.readAllLines(path).forEachIndexed { index, line ->
                        forbiddenTokens.forEach { token ->
                            if (line.contains(token)) {
                                violations += "$relativePath:${index + 1} contains forbidden token `$token`"
                            }
                        }
                    }
                }
        }

        assertTrue(
            violations.isEmpty(),
            buildString {
                appendLine(":shared main source must stay KMP-ready and platform-neutral.")
                appendLine("Move platform code to Android/Wear/iOS data or presentation layers.")
                append(violations.joinToString(separator = "\n"))
            }
        )
    }

    private fun resolveSharedMainSourceRoot(): Path {
        val currentDir = Path.of("").toAbsolutePath().normalize()
        val candidates = generateSequence(currentDir) { path -> path.parent }
            .flatMap { path ->
                sequenceOf(
                    path.resolve("shared/src/main/java"),
                    path.resolve("apps/shared/src/main/java")
                )
            }

        return candidates.firstOrNull { path -> Files.isDirectory(path) }
            ?: error("Unable to find apps/shared/src/main/java from $currentDir")
    }
}
