package spark.stock.analysis

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object AggregateFunctionByWindowJob {
  def main(args: Array[String]): Unit = {
    val streamExecutionEnvironment : StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val dataStream : DataStream[StationLog] = streamExecutionEnvironment.socketTextStream("127.0.0.1", 8888)
      .map(line => {
        var arr =line.split(",")
        new StationLog(arr(0).trim, arr(1).trim, arr(2).trim, arr(3).trim, arr(4).trim.toLong, arr(5).trim.toLong)
      })
    dataStream.map(log => ((log.sid, 1)))
      .keyBy(_._1)
      .window(SlidingProcessingTimeWindows.of(Time.seconds(5),Time.seconds(3)))
      .aggregate(new MyAggregateFunction, new MyWindowFunction)
      .print()
  }

  class MyAggregateFunction extends AggregateFunction[(String, Int), Long, Long]{
    override def createAccumulator(): Long = 0

    override def add(in: (String, Int), acc: Long): Long = acc + in._2

    override def getResult(acc: Long): Long = acc

    override def merge(acc: Long, acc1: Long): Long = acc + acc1
  }

  class MyWindowFunction extends WindowFunction[Long, (String, Long), String, TimeWindow]{
    override def apply(key: String, window: TimeWindow, input: Iterable[Long], out: Collector[(String, Long)]): Unit = {
      out.collect((key, input.iterator.next()))
    }
  }
}
