package dev.kord.cache.annotation

import dev.kord.cache.api.data.DataDescription
import com.squareup.kotlinpoet.*
import javax.lang.model.element.Element

internal data class PropertyLink(val source: Property, val target: Property)

internal data class Entity(val element: Element, val identity: Property, val links: Set<PropertyLink>) {
    private val formatted: CodeBlock = CodeBlock {
        add("description(%L)", identity.codeBlock)

        if (links.isEmpty()) return@CodeBlock

        brackets {
            links.forEach { addStatement("link(%L to %L)", it.source.codeBlock, it.target.codeBlock) }
        }
    }

    val description: PropertySpec
        get() {
            val type = with(ParameterizedTypeName.Companion) {
                DataDescription::class.asClassName().parameterizedBy(listOf(element.asType().asTypeName(), ANY))
            }

            return PropertySpec.builder("${element.simpleName.toString().decapitalize()}Description", type)
                    .initializer(formatted)
                    .build()
        }
}