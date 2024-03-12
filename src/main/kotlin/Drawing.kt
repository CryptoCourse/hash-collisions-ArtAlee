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
        val series = XYSeries("Performance Series")

        for (hashSize in 8..24) {
            val pollardMethod = PollardMethod(ShaXX(), hashSize)
            val startTime = System.currentTimeMillis()
            pollardMethod.findCollisions()
            val endTime = System.currentTimeMillis()
            val executionTime = endTime - startTime

            // Add the execution time to the series
            series.add(hashSize.toDouble(), executionTime.toDouble())
        }

        val dataset = XYSeriesCollection().apply {
            addSeries(series)
        }

        val chart = ChartFactory.createXYLineChart(
            "Time",
            "Hash Size",
            "Execution Time (ms)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        val frame = JFrame("Chart").apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            contentPane.add(ChartPanel(chart))
            pack()
            isVisible = true
        }

        // Save chart to a file
        val chartFile = File("performance_chart.png")
        ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600)
        println("Chart saved to: ${chartFile.absolutePath}")
    }
}

