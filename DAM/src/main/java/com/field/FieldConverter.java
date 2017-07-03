package com.field;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by dthien on 7/3/2017.
 */
public interface FieldConverter {

    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException;

    public Object javaToArg(FieldType fieldType, Object obj) throws SQLException;


    public Object resultToJava(FieldType fieldType, ResultSet results, int columnPos) throws SQLException;


    public SqlType getSqlType();


    public boolean isStreamType();
}