package com.guideon.plan.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StringListJsonTypeHandler extends BaseTypeHandler<List<String>> {
    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<List<String>> TYPE = new TypeReference<>(){};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        try { ps.setString(i, OM.writeValueAsString(parameter==null? new ArrayList<>() : parameter)); }
        catch (Exception e) { throw new SQLException("serialize List<String> to JSON fail", e); }
    }
    @Override public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException { return parse(rs.getString(columnName)); }
    @Override public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException { return parse(rs.getString(columnIndex)); }
    @Override public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException { return parse(cs.getString(columnIndex)); }

    private List<String> parse(String json) throws SQLException {
        try { return (json==null || json.isBlank()) ? new ArrayList<>() : OM.readValue(json, TYPE); }
        catch (Exception e) { throw new SQLException("parse JSON to List<String> fail", e); }
    }
}
