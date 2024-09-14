package me.him188.ani.utils.coroutines

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.concurrent.Volatile
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class ExceptionCollector {

    constructor()
    constructor(initial: Throwable?) {
        collect(initial)
    }

    constructor(vararg initials: Throwable?) {
        for (initial in initials) {
            collect(initial)
        }
    }

    protected open fun beforeCollect(throwable: Throwable) {
    }

    @Volatile
    private var last: Throwable? = null
    private val hashCodes = mutableSetOf<Long>()
    private val suppressedList = mutableListOf<Throwable>()
    private val lock = SynchronizedObject()

    /**
     * @return `true` if [e] is new.
     */
    fun collect(e: Throwable?): Boolean {
        synchronized(lock) {
            if (e == null) return false
            if (!hashCodes.add(hashException(e))) return false // filter out duplications
            // we can also check suppressed exceptions of [e] but actual influence would be slight.
            beforeCollect(e)
            this.last?.let { addSuppressed(e, it) }
            this.last = e
            return true
        }
    }

    protected open fun addSuppressed(receiver: Throwable, e: Throwable) {
        suppressedList.add(e)
//        receiver.addSuppressed(e)
    }

    fun collectGet(e: Throwable?): Throwable {
        this.collect(e)
        return getLast()!!
    }

    /**
     * Alias to [collect] to be used inside [withExceptionCollector]
     * @return `true` if [e] is new.
     */
    fun collectException(e: Throwable?): Boolean = collect(e)

    /**
     * Adds [suppressedList] to suppressed exceptions of [last]
     */
    private fun bake() {
        synchronized(lock) {
            last?.let { last ->
                for (suppressed in suppressedList.asReversed()) {
                    last.addSuppressed(suppressed)
                }
            }
            suppressedList.clear()
        }
    }

    fun getLast(): Throwable? {
        bake()
        return last
    }

    @TerminalOperation // to give it a color for a clearer control flow
    fun collectThrow(exception: Throwable): Nothing {
        collect(exception)
        throw getLast()!!
    }

    @TerminalOperation
    fun throwLast(): Nothing {
        throw getLast() ?: error("Internal error: expected at least one exception collected.")
    }

    @DslMarker
    private annotation class TerminalOperation

    @TestOnly // very slow
    fun asSequence(): Sequence<Throwable> {
        fun Throwable.itr(): Iterator<Throwable> {
            return (sequenceOf(this) + this.suppressedExceptions.asSequence()
                .flatMap { it.itr().asSequence() }).iterator()
        }

        val last = getLast() ?: return emptySequence()
        return Sequence { last.itr() }
    }

    fun dispose() { // help gc
        synchronized(lock) {
            this.last = null
            this.hashCodes.clear()
            this.suppressedList.clear()
        }
    }

    companion object {
        fun compressExceptions(exceptions: Array<Throwable>): Throwable? {
            return ExceptionCollector(*exceptions).getLast()
        }

        fun compressExceptions(exception: Throwable, vararg exceptions: Throwable): Throwable {
            return ExceptionCollector(exception, *exceptions).getLast()!!
        }
    }
}

/**
 * Run with a coverage of `throw`. All thrown exceptions will be caught and rethrown with [ExceptionCollector.collectThrow]
 */
inline fun <R> withExceptionCollector(action: ExceptionCollector.() -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    return ExceptionCollector().run {
        withExceptionCollector(action).also { dispose() }
    }
}

/**
 * Run with a coverage of `throw`. All thrown exceptions will be caught and rethrown with [ExceptionCollector.collectThrow]
 */
inline fun <R> ExceptionCollector.withExceptionCollector(action: ExceptionCollector.() -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    this.run {
        try {
            return action()
        } catch (e: Throwable) {
            collectThrow(e)
        } finally {
            dispose()
        }
    }
}

internal expect fun hashException(e: Throwable): Long
