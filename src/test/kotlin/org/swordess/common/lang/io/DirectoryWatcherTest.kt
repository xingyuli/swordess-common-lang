/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vic Lau
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.swordess.common.lang.io

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.StandardWatchEventKinds
import java.util.HashSet
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DirectoryWatcherTest {

    lateinit var dir: File
    lateinit var watcher: DirectoryWatcher

    @Before
    fun setUp() {
        dir = File("build/tmp/test/${javaClass.simpleName}").apply {
            if (!exists()) {
                mkdirs()
            }
        }

        watcher = DirectoryWatcher(dir.absolutePath, StandardWatchEventKinds.ENTRY_CREATE).apply { init() }
    }

    @After
    fun tearDown() {
        watcher.destroy()
        dir.listFiles().forEach { it.delete() }
    }

    @Test(timeout = 30000)
    fun testHandlerShouldBeInvokedWhenInterestedEventOccurred() {
        var invoked = false
        watcher.addHandler { invoked = true }

        File(dir, "testCreate").createNewFile()

        val thread = Thread({
            while (!invoked) { Thread.sleep(1000) }
        })
        thread.start()
        thread.join()
    }

    @Test
    fun testHandlerShouldNotBeInvokedIfFilenameExtensionNotMatch() {
        watcher.filenameExtensionInclude = ".json"

        val changes = HashSet<String>()
        watcher.addHandler { changes.add(it.change.fileName.toString()) }

        File(dir, "testCreate").createNewFile()
        File(dir, "testCreate.json").createNewFile()

        val waitFor = 30000
        var wait = 0
        val thread = Thread({
            while (wait < waitFor) { with(1000) { Thread.sleep(this@with.toLong()); wait += this@with } }
        })
        thread.start()
        thread.join()

        assertEquals(1, changes.size)
        assertTrue("testCreate.json" in changes)
    }

}