/*
 * Copyright 2019. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.jarfilter

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.google.common.collect.ImmutableSet
import org.gradle.api.Project
import java.io.File
import java.util.regex.Pattern

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class JarFilterTransform(private val project: Project) : Transform() {
    private val configFile = File(project.buildDir, UpdateConfigTask.CONFIG_FILE_NAME)

    override fun getName() = "jarFilter"

    override fun getInputTypes(): Set<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun isIncremental() = true

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = ImmutableSet.of(QualifiedContent.Scope.EXTERNAL_LIBRARIES)

    override fun getSecondaryFiles(): MutableCollection<SecondaryFile> = ImmutableSet.of(SecondaryFile.nonIncremental(project.files(configFile)))

    override fun transform(invocation: TransformInvocation) {
        val outputProvider = invocation.outputProvider!!

        val configs = Utils.getConfigsFromFile(configFile) ?: return
        val filters = configs.map {
            Pattern.compile(it.name) to JarFilter(it)
        }.toList()
        println(filters)

        if (!invocation.isIncremental) {
            outputProvider.deleteAll()
        }

        invocation.inputs.map { it.jarInputs }.flatten().forEach { jarInput ->
            if (!invocation.isIncremental) {
                copyAndFilterJar(outputProvider, jarInput, filters)
                return@forEach
            }

            when (jarInput.status) {
                Status.NOTCHANGED, null -> {
                }

                Status.ADDED, Status.CHANGED -> {
                    copyAndFilterJar(outputProvider, jarInput, filters)
                }

                Status.REMOVED -> {
                    val outJarFile = outputProvider.getContentLocation(
                            jarInput.name,
                            jarInput.contentTypes,
                            jarInput.scopes,
                            Format.JAR
                    )
                    FileUtils.deleteIfExists(outJarFile)
                }
            }
        }
    }

    private fun copyAndFilterJar(
            outputProvider: TransformOutputProvider,
            jarInput: JarInput,
            jarFilters: List<Pair<Pattern, JarFilter>>) {

        val outJarFile = outputProvider.getContentLocation(
                jarInput.name,
                jarInput.contentTypes,
                jarInput.scopes,
                Format.JAR
        )

        if(jarInput.file.name.matches(Regex("\\d+.jar"))){
            //被其他transform搞过了,已经没有包名之类的信息了
            jarFilters.forEach {
                var  filter = it.second
                Utils.copyAndFilterJar(jarInput.file, outJarFile, filter)
            }
            return
        }

        val filter = jarFilters.firstOrNull {
            val pattern = it.first
            pattern.matcher(jarInput.name).matches()
        }?.second

        Utils.copyAndFilterJar(jarInput.file, outJarFile, filter)
    }
}