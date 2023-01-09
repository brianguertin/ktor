description = ""

kotlin.sourceSets {
    commonMain {
        dependencies {
            api(project(":ktor-http"))
        }
    }

    commonTest {
        dependencies {
            api(project(":ktor-test-dispatcher"))
        }
    }
    jvmMain {
        dependencies {
            api(project(":ktor-network"))
        }
    }

    jvmTest {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
