kotlin {
    sourceSets {
        commonTest {
            dependencies {
                api(project(":ktor-test-dispatcher"))
                implementation( "org.jetbrains.kotlin:kotlin-test:1.7.10")
            }
        }
    }
}
