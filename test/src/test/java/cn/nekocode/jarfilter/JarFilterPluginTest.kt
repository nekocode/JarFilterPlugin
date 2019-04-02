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

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class JarFilterPluginTest {
    @get:Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun testApply() {
        fun assertApply(pluginClass: Class<out Plugin<*>>, expectedSuccess: Boolean) {
            val e = try {
                val project = ProjectBuilder.builder()
                        .withName("test")
                        .build() as DefaultProject
                project.plugins.apply(pluginClass)
                project.plugins.apply(JarFilterPlugin::class.java)
                null

            } catch (e: Exception) {
                e
            }

            assertTrue {
                val failed = (e?.cause is UnsupportedOperationException)
                failed != expectedSuccess
            }
        }

        assertApply(AppPlugin::class.java, true)
        assertApply(LibraryPlugin::class.java, false)
        assertApply(JavaLibraryPlugin::class.java, false)
    }

    @Test
    fun testUpdateConfig() {
        val project = ProjectBuilder.builder()
                .withProjectDir(tmpFolder.newFolder("test"))
                .withName("example")
                .build() as DefaultProject

        project.plugins.apply(AppPlugin::class.java)
        project.plugins.apply(JarFilterPlugin::class.java)

        val configs = project.extensions.getByName(JarFilterPlugin.CONFIG_KEYWORD)
                as NamedDomainObjectContainer<JarFilterConfig>

        configs.create("com.android.support:appcompat-v7:(.*)") { jarFilterConfig ->
            jarFilterConfig.excludes = setOf(
                    "android/support/v7/app/AppCompatActivity.class",
                    "android/support/v7/app/AppCompatActivity\\$(.*).class"
            )
        }

        val updateConfigTask = project.tasks.getByName(JarFilterPlugin.UPDATE_CONFIG_TASK_NAME)
                as UpdateConfigTask
        updateConfigTask.update()

        val configFile = File(project.buildDir, UpdateConfigTask.CONFIG_FILE_NAME)
        assertTrue { configFile.exists() }
        assertEquals(configs.toSet(), Utils.getConfigsFromFile(configFile)?.toSet())

        val configText = configFile.readText()
        updateConfigTask.update()
        assertEquals(configText, configFile.readText())
    }
}