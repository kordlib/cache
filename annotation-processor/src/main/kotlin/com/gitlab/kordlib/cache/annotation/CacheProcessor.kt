package com.gitlab.kordlib.cache.annotation

import com.gitlab.kordlib.cache.api.Identity
import com.gitlab.kordlib.cache.api.Link
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.asTypeName
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
private const val KORDLIB_CACHE_PACKAGE = "kordlib.cache.package"
private const val defaultPackage = "com.gitlab.kordlib.cache.api.data"

val ProcessingEnvironment.outputPackage get() = options[KORDLIB_CACHE_PACKAGE] ?: defaultPackage
val ProcessingEnvironment.outputDirectory get() = options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()

@AutoService(Processor::class)
class CacheProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(nameOf<Identity>(), nameOf<Link>())


    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val entities = buildEntities(roundEnv)
        if (entities.isEmpty()) return true
        entities.ensureLinked()

        val spec = FileSpec(processingEnv.outputPackage, "Description") {
            addDescriptionImport()
            entities.forEach {
                addProperty(it.description)
                it.identity.apply(this, processingEnv)
            }
        }

        val uri = processingEnv.outputDirectory
        val path = Paths.get(uri)
        Files.createDirectories(path)
        spec.writeTo(path)

        return true
    }

    private fun buildEntities(environment: RoundEnvironment): List<Entity> {
        val identityByClass = environment.getAnnotations<Identity>().groupByEnclosingElement()
        val linksByClass = environment.getAnnotations<Link>().groupByEnclosingElement()

        val propertyLinksByClass = linksByClass.generateLinks()
        val identityLinksByClass = identityByClass.generateIdentities()

        val allEntities = propertyLinksByClass.keys + identityByClass.keys
        return allEntities.map {
            val properties = propertyLinksByClass[it].orEmpty()
            val identity = identityLinksByClass[it] ?: run {
                val message = "every class annotated with @Link needs one @Identity annotation, but ${it.asTypeName()} did not"
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
                error(message)
            }

            Entity(processingEnv.typeUtils.asElement(it.propertyType), identity, properties.toSet())
        }
    }

    private inline fun <reified T : Any> nameOf(): String = T::class.java.name

    private inline fun <reified T : Annotation> RoundEnvironment.getAnnotations(): Set<ExecutableElement> {
        return getElementsAnnotatedWith(T::class.java) as Set<ExecutableElement>
    }

    val Link.typeMirror
        get() : TypeMirror = try {
            processingEnv.elementUtils.getTypeElement(to.java.canonicalName).asType()
        } catch (exception: MirroredTypeException) {
            exception.typeMirror
        }

    private fun Set<ExecutableElement>.groupByEnclosingElement() = groupBy {
        if (it.parameters.isNotEmpty()) it.parameters.first().asType() else it.enclosingElement.asType()
    }

    private val ExecutableElement.propertyType: TypeMirror?
        get() = if (parameters.isNotEmpty()) parameters.first().asType() else null

    private val TypeMirror.propertyType: TypeMirror
        get() = if ((this as? ExecutableElement)?.parameters?.isNotEmpty() == true) {
            parameters.first().asType()
        } else this


    private val ExecutableElement.isExtensionProperty: Boolean get() = propertyType != null

    private fun List<Entity>.ensureLinked() = forEach {
        it.links.forEach { link ->
            if (none { entity -> entity.identity.type == link.target.type }) {
                val message = "${link.target.type} is registered as a link in ${it.identity.type} but has no @Identity property"
                processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, message)

            }
        }
    }

    private fun Map<TypeMirror, List<ExecutableElement>>.generateLinks() = mapValues { (element, properties) ->
        properties.map {
            val annotation = it.getAnnotation(Link::class.java)

            val source = Property.ClassProperty(it.propertyType ?: element, it)
            val target = Property.ClassProperty(annotation.typeMirror, annotation.name)
            PropertyLink(source, target)
        }
    }

    private fun Map<TypeMirror, List<ExecutableElement>>.generateIdentities() = mapValues { (element, properties) ->
        val map = properties.map {
            val element = it.propertyType ?: element

            if (it.isExtensionProperty) Property.ExtensionProperty(element, it)
            else Property.ClassProperty(element, it)
        }
        runCatching { map.single() }.getOrElse {
            val message = "${element.asTypeName()} contains more than one @Identity, but only 1 is allowed"
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
            error(message)
        }
    }

}