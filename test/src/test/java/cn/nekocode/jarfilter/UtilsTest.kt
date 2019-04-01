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

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class UtilsTest {
    @get:Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun testGetConfigsFromFile() {
        val nonexistentFile = File(tmpFolder.newFolder(), "nonexistent")
        assertNull(Utils.getConfigsFromFile(nonexistentFile))

        val emptyFile = tmpFolder.newFile()
        assertNull(Utils.getConfigsFromFile(emptyFile))

        val jsonFile = tmpFolder.newFile()
        jsonFile.writeText("""
            [
              {
                "includes": [
                  "(.*)"
                ],
                "excludes": [
                  "(.*)"
                ],
                "name": "(.*)"
              }
            ]
        """.trimIndent())
        assertNotNull(Utils.getConfigsFromFile(jsonFile))
    }

    @Test
    fun testGetConfigsFromBytes() {
        var configs = Utils.getConfigsFromBytes("""
            [
              {
                "includes": [
                  "(.*)"
                ],
                "excludes": [
                  "(.*)"
                ],
                "name": "(.*)"
              }
            ]
        """.trimIndent().toByteArray())
        assertEquals(setOf(JarFilterConfig("(.*)", setOf("(.*)"), setOf("(.*)"))), configs)

        val e = try {
            Utils.getConfigsFromBytes("""
                [
                  {
                    "includes": [
                      "(.*)"
                    ],
                    "excludes": [
                      "(.*)"
                    ]
                  }
                ]
            """.trimIndent().toByteArray())
            null

        } catch (e: Exception) {
            e
        }
        assertTrue { e is TypeCastException }

        configs = Utils.getConfigsFromBytes("""
            [
              {
                "name": "(.*)"
              }
            ]
        """.trimIndent().toByteArray())
        assertEquals(setOf(JarFilterConfig("(.*)", emptySet(), emptySet())), configs)
    }

    @Test
    fun testCopyAndFilterJar() {
        val filter = JarFilter(JarFilterConfig("(.*)", emptySet(), setOf("(.*)b")))

        val nonexistentFile = File(tmpFolder.newFolder(), "nonexistent")
        val nonexistentFile2 = File(tmpFolder.newFolder(), "nonexistent2")
        Utils.copyAndFilterJar(nonexistentFile, nonexistentFile2, filter)
        assertTrue { !nonexistentFile.exists() }
        assertTrue { !nonexistentFile2.exists() }

        val txtFile1 = tmpFolder.newFile()
        val txtFile2 = tmpFolder.newFile()
        txtFile1.writeText("abc")
        Utils.copyAndFilterJar(txtFile1, txtFile2, null)
        assertEquals("abc", txtFile2.readText())

        val jarFile1 = tmpFolder.newFile()
        val jarFile2 = tmpFolder.newFile()
        ZipOutputStream(FileOutputStream(jarFile1)).use { zos ->
            fun putEntry(name: String) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(1)
                zos.closeEntry()
            }
            for (fileName in listOf("a", "ab", "bc")) {
                putEntry(fileName)
            }
        }
        Utils.copyAndFilterJar(jarFile1, jarFile2, filter)
        ZipInputStream(FileInputStream(jarFile2)).use { zis ->
            var i: ZipEntry?

            val actual = HashSet<String>()
            while (zis.nextEntry.let { i = it; i != null }) {
                val entry = i ?: break
                actual.add(entry.name)
            }

            assertEquals(setOf("a", "bc"), actual)
        }
    }
}