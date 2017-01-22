package io.github.K0zka.kotlinsonar.foundation

import com.google.common.base.Preconditions
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.java.DefaultJavaResourceLocator
import org.sonar.java.JavaClasspath
import org.sonar.java.JavaFilesCache
import org.sonar.plugins.java.api.JavaFileScannerContext
import org.sonar.plugins.java.api.JavaResourceLocator
import java.io.File

class KotlinJavaResourceLocator(
        private val fs : FileSystem,
        private val javaClassPath : JavaClasspath) : JavaResourceLocator {

    internal val resourcesByClass = mutableMapOf<String, InputFile>()

    var sensorContext : SensorContext? = null

    override fun findResourceByClassName(className: String): InputFile? {
        val name = className.replace('.', '/')
        return resourcesByClass.get(name)
    }

    override fun classpath(): MutableCollection<File> {
        return javaClassPath.elements
    }

    override fun classFilesToAnalyze(): Collection<File> {
        val result = mutableListOf<File>()
        classKeys()
            .map { it + ".class" }
            .forEach { filePath ->
                for (binaryDir in javaClassPath.binaryDirs) {
                    val classFile = File(binaryDir, filePath)
                    if (classFile.isFile) {
                        result.add(classFile)
                        break
                    }
                }
            }
        return result.toList()
    }

    override fun findSourceFileKeyByClassName(className: String?): String {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun classKeys() : Collection<String> {
        return resourcesByClass.keys.toSortedSet()
    }
    override fun scanFile(context: JavaFileScannerContext?) {
        Preconditions.checkNotNull(sensorContext)
        val javaFilesCache = JavaFilesCache()
        javaFilesCache.scanFile(context)
        val inputFile = fs.inputFile(fs.predicates().`is`(context.getFile())) ?: throw IllegalStateException("resource not found : " + context.getFileKey())
        for ((key) in javaFilesCache.resourcesCache) {
            resourcesByClass.put(key, inputFile)
            if (context.getFileKey() != null) {
                sourceFileByClass.put(key, context.getFileKey())
            }
        }    }
}