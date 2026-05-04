package spark.stock.analysis.job;

import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @ClassName RockdbJob
 * @Description TODO
 * @Author Spark
 * @Date 4/8/2020 10:09 AM
 **/
public class RockdbJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointInterval(10L * 1000);

        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
//		checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
//		checkpointConfig.setCheckpointTimeout(10);
//		checkpointConfig.setMinPauseBetweenCheckpoints(30000);
//		checkpointConfig.setMaxConcurrentCheckpoints(1);

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.enableCheckpointing(2000L);
        env.setStateBackend(new RocksDBStateBackend("file:///E:/RocksDB/Data", true));


        DataStreamSource<String> text = env.socketTextStream("localhost", 9999);
        text.print().setParallelism(1);
        env.execute("RockdbJob");
    }
}
