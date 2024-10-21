import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    org.jetbrains.kotlin.jvm
    org.jetbrains.kotlinx.`binary-compatibility-validator`
    org.jetbrains.dokka
    id("com.vanniktech.maven.publish.base")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dokka {
    configure(project)
}

mavenPublishing {
    configure(KotlinJvm(JavadocJar.Dokka("dokkaGeneratePublicationHtml")))
}
