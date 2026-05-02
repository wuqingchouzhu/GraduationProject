package com.qiao.demo.inventory.controller;

import com.qiao.demo.inventory.common.Result;
import com.qiao.demo.inventory.dto.StockInDTO;
import com.qiao.demo.inventory.dto.StockOutDTO;
import com.qiao.demo.inventory.mq.StockInProducer;
import com.qiao.demo.inventory.mq.StockOutProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理员接口", description = "失败消息管理、重试与出入库记录查询")
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StockOutProducer stockOutProducer;

    @Autowired
    private StockInProducer stockInProducer;

    @Operation(summary = "查询失败消息列表", description = "查询最近100条处理失败的消息记录（入库和出库）")
    @GetMapping("/failed-messages")
    public Result<List<Map<String, Object>>> listFailedMessages() {
        String sql = "SELECT id, message_type, product_id, quantity, customer_name, fail_reason, fail_time, message_id " +
                "FROM failed_messages ORDER BY fail_time DESC LIMIT 100";
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败消息出错: " + e.getMessage());
        }
    }

    @Operation(summary = "重试失败消息", description = "根据失败消息ID重新发送到MQ，支持入库和出库两种消息类型")
    @PostMapping("/retry/{id}")
    public Result<?> retryFailedMessage(@Parameter(description = "失败消息ID") @PathVariable Long id) {
        String querySql = "SELECT message_type, product_id, quantity, customer_name FROM failed_messages WHERE id = ?";
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(querySql, id);
            String messageType = (String) row.get("message_type");
            Integer productId = (Integer) row.get("product_id");
            Integer quantity = (Integer) row.get("quantity");
            String customerName = (String) row.get("customer_name");

            if ("STOCK_IN".equals(messageType)) {
                StockInDTO dto = new StockInDTO(productId, quantity);
                stockInProducer.send(dto);
                jdbcTemplate.update("DELETE FROM failed_messages WHERE id = ?", id);
                return Result.success("已重新发送入库消息: productId=" + productId);
            } else {
                StockOutDTO dto = new StockOutDTO(productId, quantity,
                        customerName != null ? customerName : "默认出库客户");
                stockOutProducer.send(dto);
                jdbcTemplate.update("DELETE FROM failed_messages WHERE id = ?", id);
                return Result.success("已重新发送出库消息: productId=" + productId);
            }
        } catch (Exception e) {
            return Result.error("重试失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询出入库记录", description = "分页查询入库和出库记录，支持按类型和商品ID筛选。入库记录显示全部，出库记录默认显示近30天。")
    @GetMapping("/stock-records")
    public Result<Map<String, Object>> listStockRecords(
            @Parameter(description = "记录类型：STOCK_IN 或 STOCK_OUT，不传则查询所有")
            @RequestParam(required = false) String type,
            @Parameter(description = "商品ID，不传则查询所有")
            @RequestParam(required = false) Integer productId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数")
            @RequestParam(defaultValue = "20") Integer size) {

        try {
            // --- build count SQL ---
            StringBuilder countSql = new StringBuilder();
            List<Object> countParams = new ArrayList<>();
            buildUnionCountQuery(countSql, countParams, type, productId);

            // --- build data SQL ---
            StringBuilder dataSql = new StringBuilder();
            List<Object> dataParams = new ArrayList<>();
            buildUnionDataQuery(dataSql, dataParams, type, productId);

            // add pagination
            int offset = (page - 1) * size;
            dataSql.append(" LIMIT ? OFFSET ?");
            dataParams.add(size);
            dataParams.add(offset);

            Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, countParams.toArray());
            List<Map<String, Object>> records = jdbcTemplate.queryForList(dataSql.toString(), dataParams.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("records", records != null ? records : List.of());
            result.put("total", total != null ? total : 0L);
            result.put("page", page);
            result.put("size", size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询出入库记录出错: " + e.getMessage());
        }
    }

    @Operation(summary = "获取仪表盘统计数据", description = "返回总商品数、低库存预警数、本月入库量、本月出库量等统计卡片数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 总商品数
            Long totalProducts = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM product_info", Long.class);
            stats.put("totalProducts", totalProducts != null ? totalProducts : 0L);

            // 低库存预警数
            Long lowStockCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM product_info WHERE alert_status = 1", Long.class);
            stats.put("lowStockCount", lowStockCount != null ? lowStockCount : 0L);

            // 本月入库量
            java.math.BigDecimal inSum = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(quantity), 0) FROM stock_in_record WHERE create_time >= DATE_FORMAT(CURDATE(), '%Y-%m-01')",
                    java.math.BigDecimal.class);
            stats.put("totalStockInThisMonth", inSum != null ? inSum.longValue() : 0L);

            // 本月出库量
            java.math.BigDecimal outSum = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(quantity), 0) FROM stock_out_record WHERE create_time >= DATE_FORMAT(CURDATE(), '%Y-%m-01')",
                    java.math.BigDecimal.class);
            stats.put("totalStockOutThisMonth", outSum != null ? outSum.longValue() : 0L);

            // 库存总价值（库存量 × 单价）
            java.math.BigDecimal val = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(stock_level * unit_price), 0) FROM product_info",
                    java.math.BigDecimal.class);
            stats.put("totalStockValue", val != null ? val : java.math.BigDecimal.ZERO);

            // 商品总数（前端便捷别名）
            stats.put("productCount", totalProducts != null ? totalProducts : 0L);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取仪表盘数据出错: " + e.getMessage());
        }
    }

    private void buildUnionCountQuery(StringBuilder sql, List<Object> params, String type, Integer productId) {
        if (type == null) {
            // count both
            sql.append("SELECT SUM(cnt) FROM (");
            sql.append("SELECT COUNT(*) AS cnt FROM stock_in_record sir");
            appendProductIdWhere(sql, params, productId, "sir");
            sql.append(" UNION ALL ");
            sql.append("SELECT COUNT(*) AS cnt FROM stock_out_record sor WHERE sor.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
            if (productId != null) {
                sql.append(" AND sor.product_id = ?");
                params.add(productId);
            }
            sql.append(") t");
        } else if ("STOCK_IN".equalsIgnoreCase(type)) {
            sql.append("SELECT COUNT(*) FROM stock_in_record sir");
            appendProductIdWhere(sql, params, productId, "sir");
        } else if ("STOCK_OUT".equalsIgnoreCase(type)) {
            sql.append("SELECT COUNT(*) FROM stock_out_record sor WHERE sor.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
            if (productId != null) {
                sql.append(" AND sor.product_id = ?");
                params.add(productId);
            }
        } else {
            // unknown type → return empty
            sql.append("SELECT 0");
        }
    }

    private void buildUnionDataQuery(StringBuilder sql, List<Object> params, String type, Integer productId) {
        if (type == null) {
            // union both
            sql.append("SELECT * FROM (");
            sql.append("SELECT sir.id, sir.product_id, p.product_name, sir.quantity, 'N/A' AS customer_name, 'STOCK_IN' AS type, ");
            sql.append("DATE_FORMAT(sir.create_time, '%Y-%m-%d %H:%i:%s') AS create_time, sir.message_id ");
            sql.append("FROM stock_in_record sir LEFT JOIN product_info p ON sir.product_id = p.id");
            appendProductIdWhere(sql, params, productId, "sir");
            sql.append(" UNION ALL ");
            sql.append("SELECT sor.id, sor.product_id, p.product_name, sor.quantity, COALESCE(sor.customer_name, '') AS customer_name, 'STOCK_OUT' AS type, ");
            sql.append("DATE_FORMAT(sor.create_time, '%Y-%m-%d %H:%i:%s') AS create_time, sor.message_id ");
            sql.append("FROM stock_out_record sor LEFT JOIN product_info p ON sor.product_id = p.id ");
            sql.append("WHERE sor.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
            if (productId != null) {
                sql.append(" AND sor.product_id = ?");
                params.add(productId);
            }
            sql.append(") t ORDER BY t.create_time DESC");
        } else if ("STOCK_IN".equalsIgnoreCase(type)) {
            sql.append("SELECT sir.id, sir.product_id, p.product_name, sir.quantity, 'N/A' AS customer_name, 'STOCK_IN' AS type, ");
            sql.append("DATE_FORMAT(sir.create_time, '%Y-%m-%d %H:%i:%s') AS create_time, sir.message_id ");
            sql.append("FROM stock_in_record sir LEFT JOIN product_info p ON sir.product_id = p.id");
            appendProductIdWhere(sql, params, productId, "sir");
            sql.append(" ORDER BY sir.create_time DESC");
        } else if ("STOCK_OUT".equalsIgnoreCase(type)) {
            sql.append("SELECT sor.id, sor.product_id, p.product_name, sor.quantity, COALESCE(sor.customer_name, '') AS customer_name, 'STOCK_OUT' AS type, ");
            sql.append("DATE_FORMAT(sor.create_time, '%Y-%m-%d %H:%i:%s') AS create_time, sor.message_id ");
            sql.append("FROM stock_out_record sor LEFT JOIN product_info p ON sor.product_id = p.id ");
            sql.append("WHERE sor.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
            if (productId != null) {
                sql.append(" AND sor.product_id = ?");
                params.add(productId);
            }
            sql.append(" ORDER BY sor.create_time DESC");
        } else {
            // unknown type → return empty
            sql.append("SELECT id, product_id, product_name, quantity, customer_name, type, create_time, message_id FROM (SELECT 1 AS id, 0 AS product_id, '' AS product_name, 0 AS quantity, '' AS customer_name, '' AS type, '' AS create_time, '' AS message_id) t WHERE 1=0");
        }
    }

    private void appendProductIdWhere(StringBuilder sql, List<Object> params, Integer productId, String alias) {
        if (productId != null) {
            sql.append(" WHERE ").append(alias).append(".product_id = ?");
            params.add(productId);
        }
    }
}
