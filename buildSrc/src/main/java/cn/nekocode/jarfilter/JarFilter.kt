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

import java.util.function.Predicate
import java.util.regex.Pattern

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class JarFilter(config: JarFilterConfig) : Predicate<String> {
    private val includes: List<Pattern> = config.includes.map { Pattern.compile(it) }
    private val excludes: List<Pattern> = config.excludes.map { Pattern.compile(it) }

    override fun test(name: String): Boolean {
        if (!includes.isEmpty()) {
            var isInclude = false
            for (pattern in includes) {
                if (pattern.matcher(name).matches()) {
                    isInclude = true
                    break
                }
            }
            if (!isInclude) {
                return false
            }
        }

        if (!excludes.isEmpty()) {
            for (pattern in excludes) {
                if (pattern.matcher(name).matches()) {
                    return false
                }
            }
        }
        return true
    }
}