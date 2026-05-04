package spark.stock.analysis

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.time.Time

object ReduceFunctionByWindowJob {
  def main(args: Array[String]): Unit = {
    val streamExecutionEnvironment : StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val dataStream : DataStream[StationLog] = streamExecutionEnvironment.socketTextStream("127.0.0.1", 8888)
      .map(line => {
        var arr =line.split(",")
        new StationLog(arr(0).trim, arr(1).trim, arr(2).trim, arr(3).trim, arr(4).trim.toLong, arr(5).trim.toLong)
      })

    dataStream.map(log => ((log.sid, 1)))
      .keyBy(_._1)
      .timeWindow(Time.seconds(5))
      .reduce((t1, t2) => (t1._1,t1._2 + t2._2))
  }
}
