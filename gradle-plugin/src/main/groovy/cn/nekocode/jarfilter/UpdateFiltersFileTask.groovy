/*
 * Copyright 2018. nekocode (nekocode.cn@gmail.com)
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
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class UpdateFiltersFileTask extends DefaultTask {
    public static final String FILTERS_FILE_NAME = 'jar-filters.json'


    @TaskAction
    void update() {
        final NamedDomainObjectContainer<JarFilter> container =
                project.extensions.getByName('jarFilters')

        final File filtersFile = new File(project.getBuildDir(), FILTERS_FILE_NAME)
        boolean needUpdate = !filtersFile.exists()

        if (!needUpdate) {
            try {
                final List list = new JsonSlurper().parse(filtersFile.readBytes())
                final Set<JarFilter> filters =
                        list.stream().map { JarFilter.fromMap(it) }.collect().toSet()

                needUpdate = (filters != container.toSet())

            } catch (Exception ignored) {
                needUpdate = true
            }
        }

        if (needUpdate) {
            Files.createParentDirs(filtersFile)
            filtersFile.write(JsonOutput.toJson(container.toArray()))
        }
    }
}
