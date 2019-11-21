package com.gitlab.kordlib.cache.annotation

import com.gitlab.kordlib.cache.api.Identity
import com.gitlab.kordlib.cache.api.Link
import com.gitlab.kordlib.cache.api.data.DataDescription
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
private const val KORDLIB_CACHE_PACKAGE = "kordlib.cache.package"
private const val defaultPath = "com.gitlab.kordlib.cache.api.data"

@AutoService(Processor::class)
class CacheProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.RELEASE_8

    private data class Property(private val clazz: TypeMirror, private val field: String) {
        constructor(clazz: TypeMirror, field: ExecutableElement) : this(clazz, field.simpleName.toString().removeSuffix("\$annotations")) //e.g.: id$annotations

        val className: String get() = clazz.asTypeName().toString()

        val codeBlock = CodeBlock.builder().add("%T::${field}", clazz).build()
    }

    private data class PropertyLink(val source: Property, val target: Property)

    private data class Entity(val element: Element, val identity: Property, val links: Set<PropertyLink>) {
        val formatted: CodeBlock
            get() {
                if (links.isEmpty()) return with(CodeBlock.Builder()) {
                    addStatement("""description(%L)""", identity.codeBlock)
                }.build()

                return with(CodeBlock.Builder()) {
                    addStatement("""description(%L){""", identity.codeBlock)
                    indent()
                    links.forEach { addStatement("link(%L to %L)", it.source.codeBlock, it.target.codeBlock) }
                    unindent()
                    add("}")
                }.build()
            }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
            nameOf<Identity>(),
            nameOf<Link>()
    )

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val fileSpec = FileSpec.builder(processingEnv.options[KORDLIB_CACHE_PACKAGE] ?: defaultPath, "Description")
                .addImport("com.gitlab.kordlib.cache.api.data", "description")

        val entities = buildEntities(roundEnv)
        entities.forEach {
            it.links.forEach { link ->
                require(entities.any { entity -> entity.identity.className == link.target.className }) {
                    "${link.target.className} is registered as a link in ${it.identity.className} but has no @Identity property"
                }
            }
        }

        if (entities.isEmpty()) return false
        entities.forEach { fileSpec.addProperty(descriptionOf(it)) }

        val uri = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        val path = Paths.get(uri)
        Files.createDirectories(path)
        fileSpec.build().writeTo(path)

        return false
    }

    val Link.typeMirror
        get() : TypeMirror {
            try {
                to.qualifiedName
            } catch (exception: MirroredTypeException) {
                return exception.typeMirror
            }
            error("class *did* exist at runtime, this can never happen")
        }

    private fun buildEntities(environment: RoundEnvironment): List<Entity> {
        val identityByClass = environment.getAnnotations<Identity>().groupBy { it.enclosingElement }
        val linksByClass = environment.getAnnotations<Link>().groupBy { it.enclosingElement }

        val propertyLinksByClass = linksByClass.mapValues { (element, properties) ->
            properties.map {
                val annotation = it.getAnnotation(Link::class.java)

                val source = Property(element.asType(), it)
                val target = Property(annotation.typeMirror, annotation.name)
                PropertyLink(source, target)
            }
        }

        val identityLinksByClass = identityByClass.mapValues { (element, properties) ->
            val map = properties.map { Property(element.asType(), it) }
            runCatching { map.single() }.onFailure {
                val message = "${element.asType().asTypeName()} contains more than one @Identity, but only 1 is allowed"
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
                error(message)
            }.getOrThrow()
        }

        val allEntities = propertyLinksByClass.keys + identityByClass.keys
        return allEntities.map {
            val properties = propertyLinksByClass[it].orEmpty()
            val identity = identityLinksByClass[it] ?: run {
                val message = "every class annotated with @Link needs one @Identity annotation, but ${it.asType().asTypeName()} did not"
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
                error(message)
            }

            Entity(it, identity, properties.toSet())
        }
    }

    private fun descriptionOf(entity: Entity): PropertySpec {
        val type = with(ParameterizedTypeName.Companion) {
            DataDescription::class.asClassName().parameterizedBy(listOf(entity.element.asType().asTypeName(), ANY))
        }
        return PropertySpec.builder("${entity.element.simpleName.toString().decapitalize()}Description", type)
                .initializer(entity.formatted)
                .build()
    }

    private inline fun <reified T : Any> nameOf(): String = T::class.java.name

    private inline fun <reified T : Annotation> RoundEnvironment.getAnnotations(): Set<ExecutableElement> {
        val elements = getElementsAnnotatedWith(T::class.java)
        return getElementsAnnotatedWith(T::class.java) as Set<ExecutableElement>
    }

}