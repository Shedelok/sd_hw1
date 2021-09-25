class LRUCache<K, V>(val capacity: Int) {
    private val key2Node = HashMap<K, Node>()

    private var head: Node? = null
    private var tail: Node? = null

    var size = 0
        private set

    init {
        require(capacity > 0) { "Capacity must be positive" }
    }

    private fun assertInvariants() {
        assert(size == key2Node.size) { "Internal error. List size is not equal to map size" }
        assert(size <= capacity) { "Internal error. Size is greater than capacity" }
        if (size == 0) {
            assert(head == null) { "Internal error. Head is not null for empty cache" }
            assert(tail == null) { "Internal error. Tail is not null for empty cache" }
        } else {
            assert(head != null) { "Internal error. Head is null for non-empty cache" }
            assert(tail != null) { "Internal error. Tail is null for non-empty cache" }
        }
    }

    private inline fun <R> withInvariantAssertions(block: () -> R): R {
        assertInvariants()
        try {
            return block()
        } finally {
            assertInvariants()
        }
    }

    fun computeIfNotPresent(key: K, computeFunction: (K) -> V): V = withInvariantAssertions {
        key2Node[key]?.let {
            ejectNode(it)
            appendNode(it)
            return@withInvariantAssertions it.value
        }

        if (size == capacity) {
            (head ?: throw NullPointerException("Internal error. Size equals capacity, but head is null")).let {
                ejectNode(it)
                key2Node.remove(it.key)
            }
        }

        val newNode = Node(key, computeFunction(key))
        key2Node[key] = newNode
        appendNode(newNode)
        return@withInvariantAssertions newNode.value
    }

    fun clear() = withInvariantAssertions {
        size = 0
        key2Node.clear()
        head = null
        tail = null
    }

    fun remove(key: K): V? = withInvariantAssertions {
        return key2Node.remove(key)?.let {
            ejectNode(it)
            it.value
        }
    }

    private fun ejectNode(node: Node) {
        if (head === node) {
            head = node.next
        }
        if (tail === node) {
            tail = node.prev
        }
        node.prev?.next = node.next
        node.next?.prev = node.prev
        node.prev = null
        node.next = null
        size--
    }

    private fun appendNode(node: Node) {
        tail.let {
            if (it == null) {
                head = node
                tail = node
            } else {
                it.next = node
                node.prev = it
                tail = node
            }
        }
        size++
    }

    private inner class Node(val key: K, val value: V) {
        var prev: Node? = null
        var next: Node? = null
    }
}