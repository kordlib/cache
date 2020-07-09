package dev.kord.cache.annotation

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec

inline fun CodeBlock(builder: CodeBlock.Builder.() -> Unit) = CodeBlock.builder().apply(builder).build()
inline fun CodeBlock.Builder.indent(builder: CodeBlock.Builder.() -> Unit) {
    indent()
    builder()
    unindent()
}

inline fun CodeBlock.Builder.brackets(builder: CodeBlock.Builder.() -> Unit) {
    addStatement("{")
    indent {
        builder()
    }
    add("}")
}

inline fun FileSpec(packageName: String, fileName: String, builder: FileSpec.Builder.() -> Unit)
        = FileSpec.builder(packageName, fileName).apply(builder).build()

fun FileSpec.Builder.addDescriptionImport() = addImport("dev.kord.cache.api.data", "description")