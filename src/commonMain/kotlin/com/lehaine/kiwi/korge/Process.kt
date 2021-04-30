package com.lehaine.kiwi.korge

import com.lehaine.kiwi.checkTrue
import com.lehaine.kiwi.korge.view.Layers
import com.soywiz.korge.view.Container
import kotlin.math.max

object ProcessManager {
    var targetFps = 60

    private val processes = mutableListOf<Process>()

    fun addProcess(process: Process) {
        processes += process
    }

    fun removeProcess(process: Process) {
        processes -= process
    }

    fun updateAll(utmod: Double) {
        processes.forEach {
            _preUpdate(it, utmod)
        }

        processes.forEach {
            _update(it)
        }

        processes.forEach {
            _fixedUpdate(it)
        }

        processes.forEach {
            _postUpdate(it)
        }

        _cleanUp(processes)
    }

    fun resizeAll() {
        processes.forEach {
            _resize(it)
        }
    }

    private fun _canRun(process: Process) = !process.paused && !process.destroyed

    private fun _preUpdate(process: Process, utmod: Double) {
        process.utmod = utmod

        if (_canRun(process)) {
            process.preUpdate()

            process.children.forEach {
                _preUpdate(it, process.utmod)
            }
        }
    }

    private fun _update(process: Process) {
        if (!_canRun(process)) return

        process.update()

        process.children.forEach {
            _update(it)
        }
    }

    private fun _fixedUpdate(process: Process) {
        if (!_canRun(process)) return

        process.run {
            fixedUpdateCounter += tmod
            while (fixedUpdateCounter >= targetFps / fixedUpdateFps) {
                fixedUpdateCounter -= targetFps / fixedUpdateFps
                fixedUpdate()
            }

            children.forEach {
                _fixedUpdate(it)
            }
        }
    }

    private fun _postUpdate(process: Process) {
        if (!_canRun(process)) return

        process.postUpdate()
        process.children.forEach {
            _postUpdate(it)
        }
    }

    private fun _resize(process: Process) {
        if (!process.destroyed) {
            process.onResize()
            process.children.forEach {
                _resize(it)
            }
        }
    }

    private fun _cleanUp(processes: MutableList<Process>) {
        var i = 0
        while (i < processes.size) {
            val process = processes[i]
            if (process.destroyed) {
                _dispose(process)
            } else {
                _cleanUp(process.children)
                i++
            }
        }
    }

    private fun _dispose(process: Process) {
        process.children.forEach {
            it.destroy()
        }
        _cleanUp(process.children)

        if (process.parent != null) {
            process.parent?.children?.remove(process)
        } else {
            processes.remove(process)
        }

        process.onDispose()
    }
}


open class Process {
    var root: Layers? = null

    var timeMultiplier = 1.0
    var utmod = 1.0
    val tmod get() = utmod * max(0.0, timeMultiplier * 1.0)

    // fixed update
    var fixedUpdateFps = 30
    var fixedUpdateCounter = 0.0

    var paused = false
    var destroyed = false
    var parent: Process? = null
    val children = mutableListOf<Process>()

    fun addTo(newParent: Process?) {
        parent = newParent
    }

    fun createRoot(container: Container) {
        checkTrue(root != null) {
            "Root already created!"
        }

        root = Layers().also {
            container.addChild(it)
        }
    }

    fun createRootAtLayer(layers: Layers, layer: Int) {
        checkTrue(root != null) {
            "Root already created!"
        }
        root = Layers().also {
            layers.add(it, layer)
        }
    }

    fun addChild(process: Process) {
        if (process.parent == null) {
            ProcessManager.removeProcess(process)
        } else {
            process.parent?.children?.remove(process)
        }

        process.parent = this
        children.add(process)
    }

    fun removeAndDestroyChild(child: Process) {
        checkTrue(child.parent != this) {
            "Not a child of this process"
        }

        child.parent = null
        children.remove(child)
        child.destroy()
    }

    fun destroy() {
        destroyed = true
    }

    fun destroyChildren() {
        children.forEach {
            it.destroy()
        }
    }

    fun isRootProcess() = parent == null

    open fun preUpdate() {}

    open fun update() {}

    open fun fixedUpdate() {}

    open fun postUpdate() {}

    open fun onResize() {}

    open fun onDispose() {}
}