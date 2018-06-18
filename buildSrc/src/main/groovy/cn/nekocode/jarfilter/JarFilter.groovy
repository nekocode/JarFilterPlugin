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

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class JarFilter {
    final String name
    private Set<String> includes = new LinkedHashSet<>()
    private Set<String> excludes = new LinkedHashSet<>()


    JarFilter(String name) {
        this.name = name
        if (name == null) throw RuntimeException("JarFilter must has a name.")
    }

    Set<String> getIncludes() {
        return includes
    }

    void setIncludes(Collection<String> includes) {
        if (includes == null) return
        this.includes = includes.toSet()
    }

    Set<String> getExcludes() {
        return excludes
    }

    void setExcludes(Collection<String> excludes) {
        if (excludes == null) return
        this.excludes = excludes.toSet()
    }

    @Override
    boolean equals(Object obj) {
        if (this.is(obj)) return true

        if (!(obj instanceof JarFilter)) return false
        final JarFilter filter = (JarFilter) obj

        if (this.name != filter.name) return false
        if (this.includes != filter.includes) return false
        if (this.excludes != filter.excludes) return false
        return true
    }

    @Override
    int hashCode() {
        return name.hashCode() +
                includes.hashCode() << 1 +
                excludes.hashCode() << 2
    }

    static JarFilter fromMap(Map<String, Object> map) {
        def name = map.name
        if (!(name instanceof String)) return null
        final JarFilter filter = new JarFilter(name)

        def includes = map.includes
        if (includes instanceof Collection<String>) {
            filter.includes = includes
        }
        def excludes = map.excludes
        if (excludes instanceof Collection<String>) {
            filter.excludes = excludes
        }

        return filter
    }
}
