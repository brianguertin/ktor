import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinCommunityPatchPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println ("applying kotlinCommunityPatch extension")
        project.extensions.create("kotlinCommunityPatch", KotlinCommunityPatchExtension::class.java)
        project.afterEvaluate {
            val extension = project.extensions.findByType(KotlinCommunityPatchExtension::class.java)

            project.buildscript.repositories.apply {
                extension.kotlinRepoUrl?.let {
                    println ("applying kotlinCommunityPatch extension: kotlinRepoUrl = $it")

                    add(maven { url = uri(it) })
                }
            }

            project.buildscript.repositories.add(project.buildscript.repositories.size(), maven {
                name = "kotlin_repo_url"
                url = uri(kotlinRepoUrl)
            })

            val kotlinVersion = extension?.kotlinVersion ?: return@afterEvaluate
            project.buildscript.dependencies.add("classpath", "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

            project.tasks.withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions.languageVersion = extension?.kotlinLanguageVersion
                it.kotlinOptions.arg = extension?.kotlinLanguageVersion
                it.kotlinOptions.freeCompilerArgs += listOf("-version")
            }
        }
    }
}

open class KotlinCommunityPatchExtension {
    var kotlinVersion: String? = null
    var kotlinLanguageVersion: String? = null
}
