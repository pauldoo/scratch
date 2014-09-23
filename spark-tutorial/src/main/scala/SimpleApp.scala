/* SimpleApp.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SimpleApp {
  def main(args: Array[String]) {
    val masterUrl = "local" // "spark://jam.lan:7077"
    val conf = new SparkConf().setMaster(masterUrl).setAppName("Simple Application")

    val logFile = "/Users/pauldoo/Programs/spark-1.1.0/README.md" // Should be some file on your system
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))

    var foobar = 0
    val wordCountsData = logData //
      .flatMap(_.split(" ")) //
      .map(w => {
        foobar += 1
        (w, 1)
      }) //
      .reduceByKey(_ + _)
    wordCountsData.persist
    val wordCounts: List[(String, Int)] = wordCountsData.collect.toList
    println(s"Wordcounts: $wordCounts")
    println(s"Foobar: $foobar")

    val averageLengthData: (Int, Int) = wordCountsData //
      .map(t => ((t._1.length() * t._2), t._2)) //
      .reduce((a, b) => ((a._1 + b._1), (a._2 + b._2)))
    val averageLength: Double = averageLengthData._1.toDouble / averageLengthData._2;
    println(s"Average length: $averageLength")
  }
}