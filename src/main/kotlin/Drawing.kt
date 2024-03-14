import kotlinx.coroutines.runBlocking
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.ChartUtils
import javax.swing.JFrame
import java.io.File

class Drawing {


    fun plot() {
        val timeSeries = XYSeries("Execution Time")
        val memorySeries = XYSeries("Memory Usage")
        val times = mutableListOf<Double>()
        val memory = mutableListOf<Double>()
        val hashSizes = mutableListOf<Int>()
        for (hashSize in 8..24) {
            val pollardMethod = PollardMethod(K = 8, STRING_SIZE = hashSize, shaXX = ShaXX(), numBit = hashSize)
            val startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val startTime = System.currentTimeMillis()
                pollardMethod.findCollisions()
            val endTime = System.currentTimeMillis()
            val memoryUsed = (Runtime.getRuntime().totalMemory() - startMemory - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
            times.add(endTime - startTime.toDouble())
            memory.add(memoryUsed.toDouble())
            hashSizes.add(hashSize)
        }
        for (i in 0 until times.size) {
            memorySeries.add(hashSizes[i], memory[i])
            timeSeries.add(hashSizes[i], times[i])
        }

        createChart(timeSeries, "Execution Time ~ Hash Size", "pollard_time_chart.png")
        createChart(memorySeries, "Memory Usage ~ Hash Size", "pollard_memory_chart.png")
    }

    fun plot2() {
        val timeSeries = XYSeries("Execution Time")
        val memorySeries = XYSeries("Memory Usage")

        for (hashSize in 8..24 step 2) {
            val birthdayParadoxMethod = BirthdayParadoxMethod(sha = ShaXX())

            val startTime = System.currentTimeMillis()
                birthdayParadoxMethod.performSearch(size = hashSize, numBit = hashSize)
            val endTime = System.currentTimeMillis()

            val runtime = Runtime.getRuntime()
            val memoryUsed = (Runtime.getRuntime().totalMemory()) / (1024 * 1024)

            timeSeries.add(hashSize.toDouble(), endTime - startTime.toDouble())
            memorySeries.add(hashSize.toDouble(), memoryUsed.toDouble())
        }

        createChart(timeSeries, "Execution Time ~ Hash size", "birth_time_chart.png")
        createChart(memorySeries, "Memory Usage ~ Hash size", "birth_memory_chart.png")
    }





    private fun createChart(series: XYSeries, chartTitle: String, fileName: String) {
        val dataset = XYSeriesCollection().apply {
            addSeries(series)
        }

        val chart = ChartFactory.createXYLineChart(
            chartTitle,
            "Hash Size",
            if (chartTitle.contains("Time")) "Execution Time (ms)" else "Memory Usage (MB)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        val frame = JFrame(chartTitle).apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            contentPane.add(ChartPanel(chart))
            pack()
            isVisible = true
        }

        val chartFile = File(fileName)
        ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600)
    }

}