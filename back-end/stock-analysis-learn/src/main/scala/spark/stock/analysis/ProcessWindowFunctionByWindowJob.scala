package spark.stock.analysis

import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.{SlidingProcessingTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object ProcessWindowFunctionByWindowJob {
  def main(args: Array[String]): Unit = {
    val streamExecutionEnvironment : StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamExecutionEnvironment.setParallelism(1)
    val dataStream : DataStream[StationLog] = streamExecutionEnvironment.socketTextStream("127.0.0.1", 8888)
      .map(line => {
        var arr =line.split(",")
        new StationLog(arr(0).trim, arr(1).trim, arr(2).trim, arr(3).trim, arr(4).trim.toLong, arr(5).trim.toLong)
      })
    dataStream.map(log => ((log.sid, 1)))
      .keyBy(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
      .process(new ProcessWindowFunction[(String, Int), (String, Long), String, TimeWindow]{
        override def process(key: String, context: Context, elements: Iterable[(String, Int)], out: Collector[(String, Long)]): Unit = {
          println("============")
          out.collect((key, elements.size))
        }
      })
      .print()

  }
}
