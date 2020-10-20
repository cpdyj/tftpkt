import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class AsyncResult<T> {
    private val locker = ReentrantLock()
    private val condition = locker.newCondition()
    var done = false
        private set
    var succeed = false
        private set
    var result: T? = null
        private set
    var cause: Throwable? = null
        private set

    fun submit(r: T) {
        locker.withLock {
            check(!done)
            done = true
            succeed = true
            result = r
            condition.signalAll()
        }
    }

    fun fail(c: Throwable) {
        locker.withLock {
            check(!done)
            done = true
            succeed = false
            result = null
            cause = c
            condition.signalAll()
        }
    }

    fun tryFail(c: Throwable) {
        locker.withLock {
            if (done) return
            done = true
            succeed = false
            result = null
            cause = c
            condition.signalAll()
        }
    }

    fun await(): T {
        locker.withLock {
            while (!done) {
                condition.await()
            }
            if (!succeed) {
                throw cause!!
            }
            return result as T
        }
    }
}
