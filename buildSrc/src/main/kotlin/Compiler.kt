import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object OptIns {
    const val coroutines = "kotlinx.coroutines.ExperimentalCoroutinesApi"
    const val serialization = "kotlinx.serialization.ExperimentalSerializationApi"
}

object Jvm {
    val target = JvmTarget.JVM_1_8
}
