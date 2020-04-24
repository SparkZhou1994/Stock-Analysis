package spark.stock.analysis

import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object CheckPointJob {
  def main(args: Array[String]): Unit = {
    val streamExecutionEnvironment : StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamExecutionEnvironment.enableCheckpointing(5000)
    streamExecutionEnvironment.setStateBackend(new FsStateBackend("hdfs://hadoop101:9000/checkpoint/cp1"))
    streamExecutionEnvironment.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    streamExecutionEnvironment.getCheckpointConfig.setCheckpointTimeout(5000)
    streamExecutionEnvironment.getCheckpointConfig.setMaxConcurrentCheckpoints(1)
    streamExecutionEnvironment.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    val dataStream: DataStream[String] = streamExecutionEnvironment.socketTextStream("127.0.0.1", 8888)
    val result: DataStream[(String, Int)] = dataStream.flatMap(_.split(" ")).map((_, 1)).keyBy(0).sum(1)
    result.print()
    streamExecutionEnvironment.execute("CheckPointJob")
  }
}
