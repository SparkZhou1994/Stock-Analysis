package spark.stock.analysis

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object SideOutputStreamJob {
  def main(args: Array[String]): Unit = {
    var notSuccessTag = OutputTag[StationLog]("not_success")

    val streamExecutionEnvironment : StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val dataStream : DataStream[StationLog] = streamExecutionEnvironment.socketTextStream("127.0.0.1", 8888)
      .map(line => {
        var arr =line.split(",")
        new StationLog(arr(0).trim, arr(1).trim, arr(2).trim, arr(3).trim, arr(4).trim.toLong, arr(5).trim.toLong)
      })
    val result: DataStream[StationLog] = dataStream.process(new CreateSideOutputStream(notSuccessTag))
    result.print("主流")
    val sideStream: DataStream[StationLog] = result.getSideOutput(notSuccessTag)
    sideStream.print("侧流")

    streamExecutionEnvironment.execute("SideOutputStreamJob")
  }

  class CreateSideOutputStream(tag: OutputTag[StationLog]) extends ProcessFunction[StationLog, StationLog]{
    override def processElement(value: StationLog, ctx: ProcessFunction[StationLog, StationLog]#Context, out: Collector[StationLog]): Unit = {
      if (value.callType.equals("success")) {
        out.collect(value)
      } else {
        ctx.output(tag,value)
      }
    }
  }
}
