package spark.stock.analysis

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object SavePointJob {
  def main(args: Array[String]): Unit = {
    val streamExecutionEnvironment : StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val dataStream: DataStream[String] = streamExecutionEnvironment.socketTextStream("127.0.0.1", 8888).uid("socket")
    val result: DataStream[(String, Int)] = dataStream.flatMap(_.split(" ")).uid("flatmap")
      .map((_, 1)).uid("map")
      .keyBy(0).
      sum(1).uid("sum")
    result.print()
    streamExecutionEnvironment.execute("SavePointJob")
  }
}
