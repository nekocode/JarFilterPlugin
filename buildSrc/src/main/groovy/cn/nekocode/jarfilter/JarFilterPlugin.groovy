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

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Debug command: ./gradlew -Dorg.gradle.daemon=false -Dorg.gradle.debug=true :example:build
 *
 * @author nekocode (nekocode.cn@gmail.com)
 */
class JarFilterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.add('jarFilters', project.container(JarFilter))

        project.getTasks()['preBuild']
                .dependsOn(project.tasks.create('updateJarFiltersFile', UpdateFiltersFileTask))

        def android = project.getExtensions().getByName("android")

        if (android != null && android instanceof AppExtension) {
            ((AppExtension) android).registerTransform(new JarFilterTransform(project))

        } else {
            throw new RuntimeException("The JarFilterPlugin can only be used in android application module.")
        }
    }
}
