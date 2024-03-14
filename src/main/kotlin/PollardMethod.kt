import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.math.BigInteger

class PollardMethod(
    private val K: Int= 8,
    private val STRING_SIZE: Int = 15,
    private val NUM_COLLISIONS: Int = 200,
    private val shaXX: ShaXX = ShaXX(),
    private val numBit: Int,
) {
    companion object {
        private val storage = ConcurrentHashMap<String, Iteration>()
        private val startValues = ConcurrentHashMap<Long, String>()
    }
    private val collisionCount = AtomicInteger(0)
    private val collisionFile = File("collisions.txt")
    private val isFound = AtomicBoolean(false)
    private val isSearching = AtomicBoolean(false)
    private val collisions = ConcurrentHashMap<String, List<String>>()
    class Iteration(
        val threadId: Long = 0,
        val numOfIteration: Int = 0

    ){
        override fun toString(): String {
            return "Thread $threadId tried $numOfIteration times"
        }
    }
    fun getStorageSize(): Int {
        return storage.size * (Long.SIZE_BYTES + Int.SIZE_BYTES + numBit)
    }
    fun checkSpecialPoint(value: String): Boolean {
        return value.substring(1, 5).toInt(2) == 0
    }

    fun Fi(hash: String): String {
        return hash + "0".repeat(K)
    }

    fun performIteration(previous: String, numOfIterations: Int) : Pair<String, Int>{
        var hash = shaXX.getSHA256Hash(previous, numBit)
        hash = Fi(hash)
        if (checkSpecialPoint(hash)) {
            if (storage.containsKey(hash)) {
                if (!isSearching.get()) {
                    isSearching.set(true)
                    found(storage[hash]!!, Iteration(Thread.currentThread().id, numOfIterations))
                    isSearching.set(false)
                }
            }
            else {
                storage[hash] = Iteration(Thread.currentThread().id, numOfIterations)
            }
        }

        return Pair(hash, numOfIterations + 1)

    }


    fun found(iter1: Iteration, iter2: Iteration) {
        val (greaterIter, lesserIter) = if (iter1.numOfIteration > iter2.numOfIteration) Pair(iter1, iter2) else Pair(iter2, iter1)
        val d = greaterIter.numOfIteration - lesserIter.numOfIteration
        var initVal1 = startValues[greaterIter.threadId]!!
        initVal1 = performFirstIterations(initVal1, d)
        var initVal2 = startValues[lesserIter.threadId]!!
        var previousVal1 = initVal1
        var previousVal2 = initVal2
        while (true) {
            if (initVal1.equals(initVal2)) {
                val image1 = BigInteger(previousVal1, 2).toString(16)
                val image2 = BigInteger(previousVal2, 2).toString(16)
                val hash1 = BigInteger(initVal1, 2).toString(16)
                val hash2 = BigInteger(initVal2, 2).toString(16)
                collisions[hash1] = collisions[hash1]?.plus(image1) ?: listOf(image1, image2)
                val collisionMessage = "Thread ${greaterIter.threadId} $image1 -> $hash1\nThread ${lesserIter.threadId} $image2 -> $hash2\n\n"
                synchronized(collisionFile) {
                    collisionFile.appendText(collisionMessage)
                }
                if (collisions.size >= NUM_COLLISIONS) {
                    isFound.set(true)
                    return
                }
            }
            previousVal1 = initVal1
            previousVal2 = initVal2
            initVal1 = performIteration(initVal1, 1).first
            initVal2 = performIteration(initVal2, 1).first
        }
    }


    fun performFirstIterations(initVal: String, d: Int): String {
        var newVal = initVal
        for (i in 0 until d) {
            newVal = shaXX.getSHA256Hash(newVal, numBit)
            newVal = Fi(newVal)
        }
        return newVal
    }

    fun findCollisions(numCollisions: Int = NUM_COLLISIONS) {
        val startTime = System.currentTimeMillis()

        val parentJob = Job()
        val jobs = List(2) {
            CoroutineScope(parentJob).launch {
                var count = 0
                var initVal = shaXX.generateRandomString(STRING_SIZE)
                startValues[Thread.currentThread().id] = initVal
                while (isActive && !isFound.get()) {
                    val result = performIteration(initVal, count)
                    initVal = result.first
                    count = result.second
                }
            }
        }

        runBlocking {
            jobs.joinAll()
        }

        if (isFound.get()) {
            parentJob.cancel()
        }

        val endTime = System.currentTimeMillis()
        val searchTime = endTime - startTime

        collisionFile.appendText("\nSearch time: $searchTime ms\n")
    }

}