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

import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class JarFilterTest {

    @Test
    fun testFilter() {
        assertFilter(
                includes = emptySet(),
                excludes = setOf("(.*)b.java"),
                src = listOf(
                        "aa.java",
                        "bb.java",
                        "cc.java",
                        "ab.java"
                ),
                expected = listOf(
                        "aa.java",
                        "cc.java"
                )
        )

        assertFilter(
                includes = emptySet(),
                excludes = setOf(
                        "(.*)b.java",
                        "(.*)c.java"
                ),
                src = listOf(
                        "aa.java",
                        "bb.java",
                        "cc.java",
                        "ab.java"
                ),
                expected = listOf(
                        "aa.java"
                )
        )

        assertFilter(
                includes = setOf("a(.*).java"),
                excludes = emptySet(),
                src = listOf(
                        "aa.java",
                        "bb.java",
                        "cc.java",
                        "ab.java"
                ),
                expected = listOf(
                        "aa.java",
                        "ab.java"
                )
        )

        assertFilter(
                includes = setOf(
                        "a(.*).java",
                        "b(.*).java"
                ),
                excludes = emptySet(),
                src = listOf(
                        "aa.java",
                        "bb.java",
                        "cc.java",
                        "ab.java"
                ),
                expected = listOf(
                        "aa.java",
                        "bb.java",
                        "ab.java"
                )
        )

        assertFilter(
                includes = setOf("a(.*).java"),
                excludes = setOf("(.*)b.java"),
                src = listOf(
                        "aa.java",
                        "bb.java",
                        "cc.java",
                        "ab.java"
                ),
                expected = listOf(
                        "aa.java"
                )
        )

        assertFilter(
                includes = setOf(
                        "a(.*).java",
                        "c(.*).java"
                ),
                excludes = setOf(
                        "(.*)b.java",
                        "(.*)a.java"
                ),
                src = listOf(
                        "aa.java",
                        "bb.java",
                        "cc.java",
                        "ab.java"
                ),
                expected = listOf(
                        "cc.java"
                )
        )
    }

    private fun assertFilter(
            includes: Set<String>, excludes: Set<String>, src: List<String>, expected: List<String>) {

        val config = JarFilterConfig("(.*)", includes, excludes)
        val filter = JarFilter(config)
        val rlt = src.filter { filter.test(it) }

        assertEquals(expected.toSet(), rlt.toSet())
    }
}