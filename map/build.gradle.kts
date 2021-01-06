import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api(api)

    testImplementation(tck)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = Jvm.target
        freeCompilerArgs = listOf(
                CompilerArguments.inlineClasses,
                CompilerArguments.coroutines,
                CompilerArguments.time,
                CompilerArguments.stdLib,
                CompilerArguments.optIn
        )
    }
}
