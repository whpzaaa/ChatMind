package org.example.chatmind.agent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DataBaseTools implements  Tool{

    private final JdbcTemplate jdbcTemplate;

    public DataBaseTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getName() {
        return "DataBaseTool";
    }

    @Override
    public String getDescription() {
        return "一个用于执行数据库查询操作的工具，主要用于从PostgreSQL中读取数据。";
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }

    /**
     * 执行数据库查询操作
     */
    @org.springframework.ai.tool.annotation.Tool(name = "databaseQuery",description = "用于在 PostgreSQL 中执行只读查询（SELECT）。接收由模型生成的查询语句，并返回结构化数据结果。该工具仅用于检索数据，严禁任何写入或修改数据库的语句。")
    public String databaseQuery(String sql) {
        try {
            String trimmedSql = sql.trim().toUpperCase();
            if (!trimmedSql.startsWith("SELECT")) {
                log.warn("拒绝执行非 SELECT 查询: {}", sql);
                return "错误：仅支持 SELECT 查询语句。提供的 SQL: " + sql;
            }

            //执行查询
            List<String> rows = jdbcTemplate.query(sql, (ResultSet rs) -> {
                List<String> resultRows = new ArrayList<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                if (columnCount == 0) {
                    resultRows.add("查询结果为空（无列）");
                    return resultRows;
                }

                //获取列名和计算每列的最大宽度
                List<String> columnNames = new ArrayList<>();
                List<Integer> columnWidths = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    columnNames.add(columnName);
                    columnWidths.add(columnName.length());
                }

                //收集所有行数据并计算列宽
                List<List<String>> dataRows = new ArrayList<>();
                while (rs.next()) {
                    List<String> rowData = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnValue = rs.getString(i);
                        rowData.add(columnValue);
                        int columnWidth = columnValue.length();
                        columnWidths.set(i - 1, Math.max(columnWidth, columnWidths.get(i - 1)));
                    }
                    dataRows.add(rowData);
                }

                //格式化表头
                StringBuilder headerBuilder = new StringBuilder();
                headerBuilder.append("|");
                for (int i = 0; i < columnCount; i++) {
                    String columnName = columnNames.get(i);
                    int width = columnWidths.get(i);
                    headerBuilder.append(String.format("%-" + width + "s", columnName)).append(" | ");
                }

                resultRows.add(headerBuilder.toString());

                //添加分割线
                StringBuilder separatorBuilder = new StringBuilder();
                separatorBuilder.append("|");
                for (int i = 0; i < columnCount; i++) {
                    int width = columnWidths.get(i);
                    separatorBuilder.append("-".repeat(width + 2)).append(" | ");
                }
                resultRows.add(separatorBuilder.toString());

                //格式化数据
                if (dataRows.isEmpty()) {
                    StringBuilder emptyBuilder = new StringBuilder();
                    emptyBuilder.append("| ");
                    int totalWidth = columnWidths.stream().mapToInt(w -> w + 3).sum() - 1;
                    emptyBuilder.append(String.format("%-" + (totalWidth - 2) + "s", "(无数据)"));
                    emptyBuilder.append(" |");
                    resultRows.add(emptyBuilder.toString());
                } else {
                    for (List<String> rowData : dataRows) {
                        StringBuilder rowBuilder = new StringBuilder();
                        rowBuilder.append("| ");
                        for (int i = 0; i < columnCount; i++) {
                            String columnValue = rowData.get(i);
                            int width = columnWidths.get(i);
                            rowBuilder.append(String.format("%-" + width + "s", columnValue)).append(" | ");
                        }
                        resultRows.add(rowBuilder.toString());
                    }
                }
                return resultRows;
            });
            int rowCount = rows.size() - 2;
            if (rowCount > 0 && rows.get(rows.size() - 1).contains("无数据")) {
                rowCount = 0;
            }

            log.info("成功执行 SQL 查询，返回 {} 行数据", rowCount);
            return "查询结果：\n" + String.join("\n", rows);
        } catch (DataAccessException e) {
            log.error("数据库查询异常: {}", e.getMessage());
            return "错误：操作失败 - " + e.getMessage() + "\nSQL: " + sql;
        }

    }
}

