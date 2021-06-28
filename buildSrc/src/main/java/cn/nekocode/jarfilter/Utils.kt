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

import com.android.utils.FileUtils
import groovy.json.JsonSlurper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
object Utils {

    fun getConfigsFromFile(configFile: File): Set<JarFilterConfig>? {
        if (configFile.exists()) {
            return try {
                Utils.getConfigsFromBytes(configFile.readBytes())
            } catch (ignored: Exception) {
                null
            }
        }
        return null
    }

    fun getConfigsFromBytes(bytes: ByteArray): Set<JarFilterConfig> {
        val list = JsonSlurper().parse(bytes) as List<Map<String, Object>>

        return list.map { objMap ->
            fun getSetField(key: String): Set<String> {
                val field = objMap[key] as Collection<String>?
                        ?: return emptySet()
                return field.toSet()
            }

            JarFilterConfig(
                    objMap["name"] as String,
                    getSetField("includes"),
                    getSetField("excludes")
            )
        }.toSet()
    }

    fun copyAndFilterJar(
            inJarFile: File,
            outJarFile: File,
            filter: JarFilter?) : Boolean {

        if (!inJarFile.exists()) {
            return false
        }

        if (filter == null) {
            FileUtils.copyFile(inJarFile, outJarFile)
            //println("filter == null,"+inJarFile.absolutePath)
            return false
        }
        //println("filter.excludes:"+filter.excludes)
        //println("filter != null,"+inJarFile.absolutePath)
        var hasTarget = false
        ZipInputStream(FileInputStream(inJarFile)).use { zis ->
            ZipOutputStream(FileOutputStream(outJarFile)).use { zos ->
                var i: ZipEntry?

                while (zis.nextEntry.let { i = it; i != null }) {
                    val entry = i ?: break

                    if (!filter.test(entry.name)) {
                        // Skip this file
                        println("file skipped success: "+entry.name +"  in jar:"+inJarFile.absolutePath)
                        hasTarget = true
                        continue
                    }

                    zos.putNextEntry(entry)
                    zis.copyTo(zos)
                    printFileCopy(inJarFile, outJarFile, entry)
                    zos.closeEntry()
                    zis.closeEntry()
                }
            }
        }
       return hasTarget;
    }

    fun copyAndFilterJarIfNameNum(
            inJarFile: File,
            outJarFile: File,
            jarFilters: List<Pair<Pattern, JarFilter>>):Boolean {

        if (!inJarFile.exists()) {
            return false
        }

        if (jarFilters.isEmpty()) {
            FileUtils.copyFile(inJarFile, outJarFile)
            //println("filter == null,"+inJarFile.absolutePath)
            return false
        }
        var hasTarget = false
        //println("filter.excludes:"+filter.excludes)
        //println("filter != null,"+inJarFile.absolutePath)
        ZipInputStream(FileInputStream(inJarFile)).use { zis ->
            ZipOutputStream(FileOutputStream(outJarFile)).use { zos ->
                var i: ZipEntry?

                while (zis.nextEntry.let { i = it; i != null }) {
                    val entry = i ?: break
                    var skip = false;

                    jarFilters.forEach {
                        var  filter = it.second
                        if (!filter.test(entry.name)) {
                            skip = true
                        }
                    }
                    if(skip){
                        // Skip this file
                        println("file skipped success: "+entry.name +"  in jar:"+inJarFile.absolutePath)
                        hasTarget = true;
                        continue
                    }
                    zos.putNextEntry(entry)
                    zis.copyTo(zos)
                    printFileCopy(inJarFile,outJarFile,entry)
                    zos.closeEntry()
                    zis.closeEntry()
                }
            }
        }
        return hasTarget
    }

    private fun printFileCopy(inJarFile: File, outJarFile: File, entry: ZipEntry) {
        //println("class copy: \n"+entry.name+"\n from "+inJarFile.absolutePath+"\n to: "+outJarFile.absolutePath+"\n")
    }
}