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

import com.google.common.io.Files
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
open class UpdateConfigTask: DefaultTask() {
    companion object {
        const val CONFIG_FILE_NAME = "jarfilter_config.json"
    }

    @TaskAction
    fun update() {
        val configs = project.extensions.getByName(JarFilterPlugin.CONFIG_KEYWORD)
                as NamedDomainObjectContainer<JarFilterConfig>

        val configFile = File(project.buildDir, CONFIG_FILE_NAME)
        var needUpdate = !configFile.exists()

        if (!needUpdate) {
            needUpdate = (Utils.getConfigsFromFile(configFile) != configs.toSet())
        }

        if (needUpdate) {
            Files.createParentDirs(configFile)
            configFile.writeText(JsonOutput.toJson(configs.toSet()))
        }
    }
}