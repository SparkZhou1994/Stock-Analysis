package spark.stock.analysis

import java.net.URL

import org.apache.flink.api.scala._


object WordCountBatchJob {
  def main(args: Array[String]): Unit = {
    val executionEnvironment: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    val dataPath: URL = getClass.getResource("/FlinkOperationDocument.txt")
    val dataSet: DataSet[String] = executionEnvironment.readTextFile(dataPath.getPath)
    val result = dataSet.flatMap(_.split(" ")).map((_, 1)).groupBy(0).sum(1)
    result.print()
  }
}
