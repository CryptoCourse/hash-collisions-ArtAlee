import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class BirthdayParadoxMethod(
    private val sha: ShaXX = ShaXX(),
    val storage: ConcurrentHashMap<String, String> = ConcurrentHashMap()
)
{
    fun insertAndCheck(hash: String, input: String): String? {
        if (storage.containsKey(hash)) {
            return storage[hash]
        }
        storage[hash] = input
        return null
    }
    fun performSearch(size: Int, numBit: Int, numThreads: Int = 4) {
        val jobs = List(numThreads) {
            GlobalScope.launch {
                var count = 0
                while (isActive) {
                    val randomString = sha.generateRandomString(size)
                    val hash = sha.getSHA256Hash(randomString, numBit)
                    val collision = insertAndCheck(hash, randomString)
                    if (collision?.isNotEmpty() == true) {
                        return@launch
                    }
                    count++
                }
            }
        }

        runBlocking {
            jobs.joinAll()
        }
    }

}