plugins {
    kotlin("multiplatform")
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        api(project(":api"))
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
    }

    jvm {
        val main by compilations.getting {
            dependencies {
                api(kotlin("test"))
                api(kotlin("test-junit"))
                api(kotlin("stdlib-jdk8"))
                api("org.junit.jupiter:junit-jupiter-api:5.5.1")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.5.1")
            }

            kotlinOptions {
                jvmTarget = "1.8"
            }

        }
    }

    js {
        val main by compilations.getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}
