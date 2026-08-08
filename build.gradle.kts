// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
}

tasks.register("installGitHooks") {
    group = "verification"
    description = "Installs git hooks"
    val projectDir = layout.projectDirectory
    doLast {
        val hooksDir = projectDir.dir(".git/hooks").asFile
        if (!hooksDir.exists()) hooksDir.mkdirs()
        val preCommit = hooksDir.resolve("pre-commit")
        preCommit.writeText(
            """
            #!/bin/bash
            echo "Running Spotless to optimize imports and format code..."
            ./gradlew spotlessApply
            status=$?
            if [ $status -ne 0 ]; then
                echo "Spotless failed. Please fix formatting issues before committing."
                exit $status
            fi
            git add .
            """.trimIndent(),
        )
        preCommit.setExecutable(true)
        println("Git pre-commit hook installed at ${preCommit.path}")
    }
}

