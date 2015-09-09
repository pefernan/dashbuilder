/**
 * Copyright (C) 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataprovider.backend.sql.dialect;

import java.util.HashMap;
import java.util.Map;

import org.dashbuilder.dataprovider.backend.sql.model.Column;
import org.dashbuilder.dataprovider.backend.sql.model.DynamicDateColumn;
import org.dashbuilder.dataprovider.backend.sql.model.Select;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.group.DateIntervalType;

/**
 * Oracle dialect for database versions greater or equals than the 12c release
 */
public class OracleDialect extends DefaultDialect {

    public static final String PATTERN_YEAR = "YYYY";
    public static final String PATTERN_MONTH = "YYYY-MM";
    public static final String PATTERN_DAY = "YYYY-MM-DD";
    public static final String PATTERN_HOUR = "YYYY-MM-DD HH24";
    public static final String PATTERN_MINUTE = "YYYY-MM-DD HH24:MI";
    public static final String PATTERN_SECOND = "YYYY-MM-DD HH24:MI:SS";

    private static Map<DateIntervalType,String> datePatternMap = new HashMap<DateIntervalType, String>();
    static {
        datePatternMap.put(DateIntervalType.SECOND, PATTERN_SECOND);
        datePatternMap.put(DateIntervalType.MINUTE, PATTERN_MINUTE);
        datePatternMap.put(DateIntervalType.HOUR, PATTERN_HOUR);
        datePatternMap.put(DateIntervalType.DAY, PATTERN_DAY);
        datePatternMap.put(DateIntervalType.WEEK, PATTERN_DAY);
        datePatternMap.put(DateIntervalType.MONTH, PATTERN_MONTH);
        datePatternMap.put(DateIntervalType.QUARTER, PATTERN_MONTH);
        datePatternMap.put(DateIntervalType.YEAR, PATTERN_YEAR);
        datePatternMap.put(DateIntervalType.DECADE, PATTERN_YEAR);
        datePatternMap.put(DateIntervalType.CENTURY, PATTERN_YEAR);
        datePatternMap.put(DateIntervalType.MILLENIUM, PATTERN_YEAR);
    }

    @Override
    public String getColumnTypeSQL(Column column) {
        switch (column.getType()) {
            case NUMBER: {
                return "NUMERIC(28,2)";
            }
            case DATE: {
                return "TIMESTAMP";
            }
            default: {
                return "VARCHAR2(" + column.getLength() + ")";
            }
        }
    }

    @Override
    public String getColumnCastSQL(Column column) {
        String columnSQL = getColumnSQL(column);
        int length = column.getLength() < 10 ? 10 : column.getLength();
        return "CAST(" + columnSQL + " AS VARCHAR2(" + length + "))";
    }

    @Override
    public String getDynamicDateColumnSQL(DynamicDateColumn column) {
        DateIntervalType type = column.getDateType();
        if (!datePatternMap.containsKey(type)) {
            throw new IllegalArgumentException("Group '" + column.getName() +
                    "' by the given date interval type is not supported: " + type);
        }
        String datePattern = datePatternMap.get(type);
        String columnName = getColumnNameSQL(column.getName());
        return "TO_CHAR(" + columnName + ", '" + datePattern + "')";
    }

    @Override
    public String getOffsetLimitSQL(Select select) {
        // Leverage the new limit clauses introduced in Oracle 12c
        int offset = select.getOffset();
        int limit = select.getLimit();
        StringBuilder out = new StringBuilder();
        if (offset > 0) out.append(" OFFSET ").append(offset).append(" ROWS");
        if (limit > 0) out.append(" FETCH FIRST ").append(limit).append(" ROWS ONLY");
        return out.toString();
    }
}