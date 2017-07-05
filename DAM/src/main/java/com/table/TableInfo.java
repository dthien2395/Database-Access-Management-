package com.table;

import com.db.DatabaseType;
import com.field.FieldType;
import com.misc.SqlExceptionUtil;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dthien on 7/3/2017.
 */
public class TableInfo<T> {
    private final Class<T> dataClass;
    private final String tableName;
    private final FieldType[] fieldTypes;
    private final Map<String, FieldType> fieldNameMap = new HashMap<String, FieldType>();
    private final FieldType idField;
    private final Constructor<T> constructor;

    public TableInfo(DatabaseType databaseType, Class<T> dataClass) throws SQLException {
        this(databaseType, DatabaseTableConfig.fromClass(databaseType, dataClass));
    }

    public TableInfo(DatabaseType databaseType, DatabaseTableConfig<T> tableConfig) throws SQLException {
        this.dataClass = tableConfig.getDataClass();
        this.tableName = tableConfig.getTableName();
        this.fieldTypes = tableConfig.extractFieldTypes(databaseType);
        // find the id field
        FieldType findIdFieldType = null;
        for (FieldType fieldType : fieldTypes) {
            if (fieldType.isId()) {
                if (findIdFieldType != null) {
                    throw new IllegalArgumentException("More than 1 idField configured for class " + dataClass + " ("
                            + findIdFieldType + "," + fieldType + ")");
                }
                findIdFieldType = fieldType;
            }
            fieldNameMap.put(fieldType.getDbColumnName(), fieldType);
        }
        // if we just have 1 field and it is a generated-id then inserts will be blank which is not allowed.
        if (fieldTypes.length == 1 && findIdFieldType != null && findIdFieldType.isGeneratedId()) {
            throw new IllegalArgumentException("Must have more than a single field which is a generated-id for class "
                    + dataClass);
        }
        // can be null if there is no id field
        this.idField = findIdFieldType;
        this.constructor = findNoArgConstructor(dataClass);
    }


    public Class<T> getDataClass() {
        return dataClass;
    }

    public String getTableName() {
        return tableName;
    }

    public FieldType[] getFieldTypes() {
        return fieldTypes;
    }

    public FieldType getFieldTypeByName(String columnName) {
        return fieldNameMap.get(columnName);
    }

    public FieldType getIdField() {
        return idField;
    }

    public String objectToString(T object) {
        StringBuilder sb = new StringBuilder();
        sb.append(object.getClass().getSimpleName());
        for (FieldType fieldType : fieldTypes) {
            sb.append(' ').append(fieldType.getDbColumnName()).append("=");
            try {
                sb.append(fieldType.getConvertedFieldValue(object));
            } catch (Exception e) {
                throw new IllegalStateException("Could not generate toString of field " + fieldType, e);
            }
        }
        return sb.toString();
    }

    public T createObject() throws SQLException {
        boolean accessible = constructor.isAccessible();
        try {
            if (!accessible) {
                constructor.setAccessible(true);
            }
            // create our instance
            return constructor.newInstance();
        } catch (Exception e) {
            throw SqlExceptionUtil.create("Could not create object for " + dataClass, e);
        } finally {
            if (!accessible) {
                constructor.setAccessible(false);
            }
        }
    }

    private Constructor<T> findNoArgConstructor(Class<T> dataClass) {
        Constructor<T>[] constructors;
        try {
            @SuppressWarnings("unchecked")
            Constructor<T>[] consts = (Constructor<T>[]) dataClass.getDeclaredConstructors();
            // i do this [grossness] to be able to move the Suppress inside the method
            constructors = consts;
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't lookup declared constructors for " + dataClass, e);
        }
        for (Constructor<T> con : constructors) {
            if (con.getParameterTypes().length == 0) {
                return con;
            }
        }
        throw new IllegalArgumentException("Can't find a no-arg constructor for " + dataClass);
    }
}
