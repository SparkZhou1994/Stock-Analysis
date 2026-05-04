package spark.stock.analysis

import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

object ProcessFunctionJob {
  def main(args: Array[String]): Unit = {
    val streamExecutionEnvironment : StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val dataStream : DataStream[StationLog] = streamExecutionEnvironment.socketTextStream("127.0.0.1", 8888)
      .map(line => {
        var arr =line.split(",")
        new StationLog(arr(0).trim, arr(1).trim, arr(2).trim, arr(3).trim, arr(4).trim.toLong, arr(5).trim.toLong)
      })
    val result: DataStream[String] = dataStream.keyBy(_.callIn).process(new MonitorCallFail)
    result.print()
    streamExecutionEnvironment.execute("ProcessFunctionJob")
  }
  class MonitorCallFail extends KeyedProcessFunction[String, StationLog, String]{
    lazy val timeState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("time", classOf[Long]))

    override def processElement(value: StationLog, ctx: KeyedProcessFunction[String, StationLog, String]#Context, out: Collector[String]): Unit = {
      var time = timeState.value()
      if (time == 0 && value.callType.equals("fail")){
        var nowTime = ctx.timerService().currentProcessingTime()
        var onTime = nowTime + 5*1000L
        ctx.timerService().registerProcessingTimeTimer(onTime)
        timeState.update(onTime)
      }
      if (time != 0 && !value.callType.equals("fail")){
        ctx.timerService().deleteProcessingTimeTimer(time)
        timeState.clear()
      }
    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, StationLog, String]#OnTimerContext, out: Collector[String]): Unit = {
      var warnings = "触发时间：" + timestamp + "，手机号：" + ctx.getCurrentKey
      out.collect(warnings)
      timeState.clear()
    }
  }
}

