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
class UpdateFilterFilesTask extends DefaultTask {
    public static final String INCLUDES_FILE_NAME = 'jar-filter-includes.txt'
    public static final String EXCLUDES_FILE_NAME = 'jar-filter-excludes.txt'


    @TaskAction
    void update() {
        final JarFilterExtension extension =
                project.extensions.getByName('jarFilter')

        updateSetFile(extension.includes,
                new File(project.getBuildDir(), INCLUDES_FILE_NAME))
        updateSetFile(extension.excludes,
                new File(project.getBuildDir(), EXCLUDES_FILE_NAME))
    }

    private static void updateSetFile(Set<String> set, File setFile) {
        final Set<String> setToSave =
                set.stream()
                        .filter { line -> !line.isAllWhitespace() }
                        .collect().toSet()

        boolean needUpdate = !setFile.exists()

        if (!needUpdate) {
            final Set<String> setFromFile =
                    setFile.collect().stream()
                            .filter { line -> !line.isAllWhitespace() }
                            .collect().toSet()

            needUpdate = (setToSave != setFromFile)
        }

        if (needUpdate) {
            Files.createParentDirs(setFile)
            setFile.withWriter('utf-8') { writer ->
                for (String line : setToSave) {
                    writer.write(line + '\n')
                }
            }
        }
    }
}
