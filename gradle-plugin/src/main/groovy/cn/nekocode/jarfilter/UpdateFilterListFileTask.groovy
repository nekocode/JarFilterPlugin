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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class UpdateFilterListFileTask extends DefaultTask {
    public static final String FILTER_FILE_NAME = 'jar-filter-list.txt'


    @TaskAction
    void update() {
        final JarFilterExtension extension =
                project.extensions.getByName('jarFilter')
        final List<String> list =
                extension.skipFiles.stream()
                        .filter { line -> !line.isAllWhitespace() }
                        .collect()

        final File listFile = new File(project.getBuildDir(), FILTER_FILE_NAME)
        boolean needUpdate = !listFile.exists()

        if (!needUpdate) {
            final List<String> listInFile =
                    listFile.collect().stream()
                            .filter { line -> !line.isAllWhitespace() }
                            .collect()

            needUpdate = (list.toSet() != listInFile.toSet())
        }

        if (needUpdate) {
            Files.createParentDirs(listFile)
            listFile.withWriter('utf-8') { writer ->
                for (String line : list) {
                    writer.write(line + '\n')
                }
            }
        }
    }
}
