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

import org.gradle.internal.Pair

import java.util.regex.Pattern

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class JarFilters {
    private final List<Pair<Pattern, FileFilter>> mathcers = new ArrayList<>()


    JarFilters(Set<JarFilter> filters) {
        for (JarFilter filter : filters) {
            mathcers.add(
                    Pair.of(Pattern.compile(filter.name), new FileFilter(filter)))
        }
    }

    FileFilter getFileFilter(String jarName) {
        final List<FileFilter> matchedMatchers = mathcers.stream()
                .filter { it.left().matcher(jarName).matches() }
                .map { it.right() }
                .collect()

        if (matchedMatchers.size() == 0) {
            return null
        } else {
            return new FileFilter(matchedMatchers)
        }
    }


    static class FileFilter {
        private Set<Pattern> includes = new LinkedHashSet<>()
        private Set<Pattern> excludes = new LinkedHashSet<>()


        FileFilter(JarFilter filter) {
            includes.addAll(
                    filter.includes.stream().map { Pattern.compile(it) }.collect()
            )
            excludes.addAll(
                    filter.excludes.stream().map { Pattern.compile(it) }.collect()
            )
        }

        FileFilter(Collection<FileFilter> matchers) {
            for (FileFilter matcher : matchers) {
                includes.addAll(matcher.includes)
                excludes.addAll(matcher.excludes)
            }
        }

        boolean test(String path) {
            boolean isInclude = includes.isEmpty()

            if (!isInclude) {
                for (Pattern pattern : includes) {
                    if (pattern.matcher(path).matches()) {
                        isInclude = true
                        break
                    }
                }
                if (!isInclude) return false
            }

            if (excludes.isEmpty()) return true
            for (Pattern pattern : excludes) {
                if (pattern.matcher(path).matches()) {
                    return false
                }
            }
            return true
        }
    }
}
