import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class LRUCacheTest {
    private fun <K, V> assertComputeFunctionCalled(cache: LRUCache<K, V>, key: K, computeFunction: (K) -> V) {
        var called = false
        cache.computeIfNotPresent(key) {
            called = true
            computeFunction(it)
        }
        assertTrue(called)
    }

    private fun <K, V> assertComputeFunctionNotCalledForKey(cache: LRUCache<K, V>, key: K): V =
        cache.computeIfNotPresent(key) { fail("Compute function called") }

    private fun <K> assertContainsKey(cache: LRUCache<K, *>, key: K) = assertComputeFunctionNotCalledForKey(cache, key)

    @Test
    fun storesCorrectValues() {
        val cache = LRUCache<Int, String>(10)

        cache.computeIfNotPresent(1) { "one" }
        cache.computeIfNotPresent(10) { "ten" }
        cache.computeIfNotPresent(20) { "twenty" }
        cache.computeIfNotPresent(21) { "twenty one" }

        assertEquals("twenty", assertComputeFunctionNotCalledForKey(cache, 20))
        assertEquals("ten", assertComputeFunctionNotCalledForKey(cache, 10))
        assertEquals("one", assertComputeFunctionNotCalledForKey(cache, 1))
        assertEquals("twenty one", assertComputeFunctionNotCalledForKey(cache, 21))
    }

    @Test
    fun doesNotExceedCapacity() {
        val cache = LRUCache<Int, Int>(2)

        assertEquals(0, cache.size)

        assertComputeFunctionCalled(cache, 0) { 0 }
        assertEquals(1, cache.size)

        assertComputeFunctionCalled(cache, 1) { 0 }
        assertEquals(2, cache.size)

        assertComputeFunctionCalled(cache, 2) { 0 }
        assertEquals(2, cache.size)
    }

    @Test
    fun doesNotCallComputeFunctionForCachedKeys() {
        val cache = LRUCache<Int, Int>(2)
        assertComputeFunctionCalled(cache, 0) { 0 }

        assertComputeFunctionNotCalledForKey(cache, 0)
    }

    @Test
    fun removesLeastRecentlyUsedWhenExceedsCapacity() {
        val cache = LRUCache<Int, Int>(4)
        assertComputeFunctionCalled(cache, 0) { 0 }
        assertComputeFunctionCalled(cache, 1) { 0 }
        assertComputeFunctionCalled(cache, 2) { 0 }
        assertComputeFunctionCalled(cache, 3) { 0 }

        assertComputeFunctionNotCalledForKey(cache, 2)
        assertComputeFunctionNotCalledForKey(cache, 0)

        // now LRU are 1, 3, 2, 0
        assertComputeFunctionCalled(cache, 4) { 0 }
        // 3, 2, 0, 4
        assertComputeFunctionCalled(cache, 1) { 0 }
        // 2, 0, 4, 1
        assertComputeFunctionCalled(cache, 3) { 0 }

        // 0, 4, 1, 3
        assertComputeFunctionNotCalledForKey(cache, 0)
        // 4, 1, 3, 0
        assertComputeFunctionCalled(cache, 5) { 0 }
        // 1, 3, 0, 5
        assertComputeFunctionCalled(cache, 4) { 0 }
    }

    @Test
    fun clear() {
        val cache = LRUCache<Int, Int>(4)

        assertComputeFunctionCalled(cache, 0) { 0 }
        assertComputeFunctionCalled(cache, 1) { 0 }
        assertComputeFunctionCalled(cache, 2) { 0 }

        assertComputeFunctionNotCalledForKey(cache, 0)

        cache.clear()

        assertEquals(0, cache.size)
        assertComputeFunctionCalled(cache, 0) { 0 }
    }

    @Test
    fun clearAfterCapacityExceed() {
        val cache = LRUCache<Int, Int>(2)

        assertComputeFunctionCalled(cache, 0) { 0 }
        assertComputeFunctionCalled(cache, 1) { 0 }
        assertComputeFunctionCalled(cache, 2) { 0 }

        assertComputeFunctionNotCalledForKey(cache, 2)

        cache.clear()

        assertEquals(0, cache.size)
        assertComputeFunctionCalled(cache, 2) { 0 }
    }

    @Test
    fun remove() {
        val cache = LRUCache<Int, String>(2)

        assertComputeFunctionCalled(cache, 0) { "zero" }
        assertComputeFunctionCalled(cache, 1) { "one" }

        assertEquals(2, cache.size)

        assertEquals("zero", cache.remove(0))

        assertEquals(1, cache.size)
    }
}