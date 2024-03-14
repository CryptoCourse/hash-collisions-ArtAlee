import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class BirthdayParadoxMethod(
    private val sha: ShaXX = ShaXX(),
    val storage: HashMap<String, String> = HashMap(),
    val collisions : HashMap<String, List<String>> = HashMap()
)
{
    fun insertAndCheck(hash: String, input: String): String? {
        if (storage.containsKey(hash)) {
            return storage[hash]
        }
        storage[hash] = input
        collisions[hash] = collisions[hash]?.plus(input) ?: listOf(input)
        return null
    }
    fun performSearch(size: Int, numBit: Int) {
        var collisionCount = 0
        while (collisions.size < 100){
            val randomString = sha.generateRandomString(size)
            val hash = sha.getSHA256Hash(randomString, numBit)
            val collision = insertAndCheck(hash, randomString)
           // if (collision?.isNotEmpty() == true) {
           //     collisionCount++
           // }
        }
    }
}

