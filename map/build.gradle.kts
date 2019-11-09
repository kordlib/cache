plugins {
    kotlin("multiplatform")
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        api(project(":api"))
    }

    sourceSets.commonTest.get().dependencies {
        implementation(project(":tck"))
    }

    jvm()
    js()
}
