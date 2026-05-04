
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class test-api-simple {
    private static final String BASE_URL = "https://push2.eastmoney.com";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        System.out.println("测试东方财富API连接...");

        try {
            // 测试连接
            boolean connected = testConnection();
            System.out.println("API连接状态: " + (connected ? "正常" : "失败"));

            if (connected) {
                // 测试获取上证指数
                testFetchStockData("sh000001", "上证指数");

                // 测试获取深证成指
                testFetchStockData("sz399001", "深证成指");

                // 测试获取贵州茅台
                testFetchStockData("sh600519", "贵州茅台");
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean testConnection() {
        try {
            URL url = new URL(BASE_URL + "/api/qt/stock/get?secid=sh000001&fields=f43");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void testFetchStockData(String secid, String name) {
        System.out.println("\n=== 获取" + name + "实时数据 ===");
        try {
            String urlStr = BASE_URL + "/api/qt/stock/get?secid=" + secid +
                           "&fields=f43,f44,f45,f46,f47,f48,f57,f58,f60,f84,f85,f86,f169,f170";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            // 解析JSON
            JsonNode root = objectMapper.readTree(content.toString());
            JsonNode data = root.get("data");

            if (data == null) {
                System.out.println("获取数据失败，响应: " + content.toString());
                return;
            }

            // 输出字段
            System.out.println("股票代码: " + secid);
            System.out.println("股票名称: " + data.get("f58").asText());
            System.out.println("当前价格: " + formatNumber(data.get("f43")));
            System.out.println("涨跌额: " + formatNumber(data.get("f170")));
            System.out.println("涨跌幅: " + formatNumber(data.get("f44")) + "%");
            System.out.println("最高价格: " + formatNumber(data.get("f44")));
            System.out.println("最低价格: " + formatNumber(data.get("f45")));
            System.out.println("开盘价格: " + formatNumber(data.get("f46")));
            System.out.println("昨收价格: " + formatNumber(data.get("f60")));
            System.out.println("成交量: " + formatNumber(data.get("f47")) + "手");
            System.out.println("成交额: " + formatNumber(data.get("f48")) + "元");
            System.out.println("换手率: " + formatNumber(data.get("f168")) + "%");
            System.out.println("更新时间: " + LocalDateTime.now());

        } catch (Exception e) {
            System.err.println("获取" + name + "数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String formatNumber(JsonNode node) {
        if (node == null || node.isNull()) {
            return "N/A";
        }
        try {
            if (node.isNumber()) {
                return node.asText();
            }
            return node.asText();
        } catch (Exception e) {
            return "N/A";
        }
    }
}
