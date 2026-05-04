package spark.stock.analysis
// 2. 导入隐式转换
import org.apache.flink.streaming.api.scala._

object WordCountStreamJob {
  def main(args: Array[String]): Unit = {
    // 1. 初始环境
    val streamExecutionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    // 3. 读取数据
    val dataStream: DataStream[String] = streamExecutionEnvironment.socketTextStream("127.0.0.1", 8888)
    // 4. 处理数据
    val result: DataStream[(String, Int)] = dataStream.flatMap(_.split(" ")).map((_, 1)).keyBy(0).sum(1)
    // 5. 输出结果
    result.print()
    // 6. 启动计算
    streamExecutionEnvironment.execute("WordCountStreamJob")
  }
}
