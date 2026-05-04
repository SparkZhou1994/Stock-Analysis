package spark.stock.analysis

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.util.Collector

object KeyStateJob {
  def main(args: Array[String]): Unit = {
    val streamExecutionEnvironment : StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val dataStream : DataStream[StationLog] = streamExecutionEnvironment.socketTextStream("127.0.0.1", 8888)
      .map(line => {
        var arr =line.split(",")
        new StationLog(arr(0).trim, arr(1).trim, arr(2).trim, arr(3).trim, arr(4).trim.toLong, arr(5).trim.toLong)
      })
    // 第一种方法
    /*dataStream.keyBy(_.callOut).flatMap(new CallIntervalFunction).print()*/
    // 第二种方法
    dataStream.keyBy(_.callOut).mapWithState[(String,Long), StationLog]{
      case (in: StationLog, None) => ((in.callOut, 0) , Some(in))
      case (in: StationLog, pre: Some[StationLog]) => {
        var interval = Math.abs(in.callTime - pre.get.callTime)
        ((in.callOut, interval), Some(in))
      }
    }
      .filter(_._2 != 0)
      .print()
    streamExecutionEnvironment.execute("KeyStateJob")
  }

  class CallIntervalFunction extends RichFlatMapFunction[StationLog,(String,Long)]{
      private var preCallTimeState: ValueState[Long] = _

    override def open(parameters: Configuration): Unit = {
      preCallTimeState = getRuntimeContext.getState(new ValueStateDescriptor[Long]("pre", classOf[Long]){})
    }

    override def flatMap(in: StationLog, collector: Collector[(String, Long)]): Unit = {
      var preCallTime = preCallTimeState.value()
      if (preCallTime == null || preCallTime == 0) {
        preCallTimeState.update(in.callTime)
      } else {
        var interval = in.callTime - preCallTime
        collector.collect((in.callOut, interval))
      }
    }
  }
}
