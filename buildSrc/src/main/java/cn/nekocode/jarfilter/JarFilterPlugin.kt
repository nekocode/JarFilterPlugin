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

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Debug: ./gradlew :e:build -Dorg.gradle.daemon=false -Dorg.gradle.debug=true
 * @author nekocode (nekocode.cn@gmail.com)
 */
class JarFilterPlugin : Plugin<Project> {
    companion object {
        const val CONFIG_KEYWORD = "jarFilters"
        const val UPDATE_CONFIG_TASK_NAME = "updateConfig"
    }

    override fun apply(project: Project) {
        // Add config object
        val jarFilters = project.container(JarFilterConfig::class.java) { name ->
            JarFilterConfig(name, emptySet(), emptySet())
        }
        project.extensions.add(CONFIG_KEYWORD, jarFilters)

        val android = project.extensions.findByName("android") as BaseExtension?
        if (android != null && android is BaseAppModuleExtension) {
            // Register transform
            android.registerTransform(JarFilterTransform(project))

            // Create a task to save config to json file before build
            val updateTask = project.tasks.create(
                    UPDATE_CONFIG_TASK_NAME, UpdateConfigTask::class.java)
            project.tasks.getByName("preBuild").dependsOn(updateTask)

        } else {
            throw UnsupportedOperationException(
                    "The JarFilterPlugin can only be used in android application module.")
        }
    }
}