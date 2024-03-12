import java.math.BigInteger
import java.security.MessageDigest

class ShaXX(
) {
    fun generateRandomString(size: Int): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..size)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
    fun getSHA256Hash(input: String, numBit: Int): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        val binaryString = BigInteger(1, hash).toString(2).padStart(256, '0')
        return binaryString.takeLast(numBit)

    }

}