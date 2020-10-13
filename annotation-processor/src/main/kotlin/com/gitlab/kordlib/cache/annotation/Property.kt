package com.gitlab.kordlib.cache.annotation

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror

internal sealed class Property {
    abstract val type: TypeMirror
    abstract val codeBlock: CodeBlock

    open fun apply(spec: FileSpec.Builder, env: ProcessingEnvironment) {}

    data class ClassProperty(override val type: TypeMirror, private val property: String) : Property() {
        constructor(type: TypeMirror, field: ExecutableElement) : this(type, field.simpleName.toString().removeSuffix("\$annotations")) //e.g.: id$annotations


        override val codeBlock = CodeBlock.builder().add("%T::${property.removePrefix("get").decapitalize()}", type).build()
    }

    data class ExtensionProperty(override val type: TypeMirror, private val property: ExecutableElement) : Property() {

        override val codeBlock get() = CodeBlock.builder().add("%T::${property.simpleName.toString().removeSuffix("\$annotations").removePrefix("get").decapitalize()}", type).build()

        override fun apply(spec: FileSpec.Builder, env: ProcessingEnvironment) {
            val packageElement = env.typeUtils.asElement(type).enclosingPackage
            spec.addImport(packageElement.toString(), property.simpleName.toString().removeSuffix("\$annotations").removePrefix("get").decapitalize())
        }

        private val Element.enclosingPackage: Element
            get() {
                var element = this
                while (element.kind != ElementKind.PACKAGE) {
                    element = element.enclosingElement
                }
                return element
            }
    }
}