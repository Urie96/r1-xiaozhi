package org.litepal.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.Const;

/* JADX INFO: loaded from: classes.dex */
public class DBUtility {
    private DBUtility() {
    }

    public static String getTableNameByClassName(String className) {
        if (TextUtils.isEmpty(className) || '.' == className.charAt(className.length() - 1)) {
            return null;
        }
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public static List<String> getTableNameListByClassNameList(List<String> classNames) {
        List<String> tableNames = new ArrayList<>();
        if (classNames != null && !classNames.isEmpty()) {
            for (String className : classNames) {
                tableNames.add(getTableNameByClassName(className));
            }
        }
        return tableNames;
    }

    public static String getTableNameByForeignColumn(String foreignColumnName) {
        if (TextUtils.isEmpty(foreignColumnName) || !foreignColumnName.toLowerCase().endsWith("_id")) {
            return null;
        }
        return foreignColumnName.substring(0, foreignColumnName.length() - "_id".length());
    }

    public static String getIntermediateTableName(String tableName, String associatedTableName) {
        if (!TextUtils.isEmpty(tableName) && !TextUtils.isEmpty(associatedTableName)) {
            if (tableName.toLowerCase().compareTo(associatedTableName.toLowerCase()) <= 0) {
                String intermediateTableName = String.valueOf(tableName) + "_" + associatedTableName;
                return intermediateTableName;
            }
            String intermediateTableName2 = String.valueOf(associatedTableName) + "_" + tableName;
            return intermediateTableName2;
        }
        return null;
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0033, code lost:
    
        r11 = r8.getInt(r8.getColumnIndexOrThrow(org.litepal.util.Const.TableSchema.COLUMN_TYPE));
     */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x003d, code lost:
    
        if (r11 != 1) goto L19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0045, code lost:
    
        return true;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean isIntermediateTable(String tableName, SQLiteDatabase db) {
        if (!TextUtils.isEmpty(tableName) && tableName.matches("[0-9a-zA-Z]+_[0-9a-zA-Z]+")) {
            Cursor cursor = null;
            try {
                try {
                    cursor = db.query(Const.TableSchema.TABLE_NAME, null, null, null, null, null, null);
                    if (cursor.moveToFirst()) {
                        while (true) {
                            String tableNameDB = cursor.getString(cursor.getColumnIndexOrThrow(Const.TableSchema.COLUMN_NAME));
                            if (tableName.equalsIgnoreCase(tableNameDB)) {
                                break;
                            }
                            if (!cursor.moveToNext()) {
                                break;
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return false;
    }

    public static boolean isTableExists(String tableName, SQLiteDatabase db) {
        try {
            boolean exist = BaseUtility.containsIgnoreCases(findAllTableNames(db), tableName);
            return exist;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isColumnExists(String columnName, String tableName, SQLiteDatabase db) {
        if (TextUtils.isEmpty(columnName) || TextUtils.isEmpty(tableName)) {
            return false;
        }
        boolean exist = false;
        Cursor cursor = null;
        try {
            try {
                String checkingColumnSQL = "pragma table_info(" + tableName + ")";
                cursor = db.rawQuery(checkingColumnSQL, null);
                if (cursor.moveToFirst()) {
                    while (true) {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(Const.TableSchema.COLUMN_NAME));
                        if (columnName.equalsIgnoreCase(name)) {
                            exist = true;
                            break;
                        }
                        if (!cursor.moveToNext()) {
                            break;
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                    return exist;
                }
                return exist;
            } catch (Exception e) {
                e.printStackTrace();
                if (cursor == null) {
                    return false;
                }
                cursor.close();
                return false;
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public static List<String> findAllTableNames(SQLiteDatabase db) {
        List<String> tableNames = new ArrayList<>();
        Cursor cursor = null;
        try {
            try {
                cursor = db.rawQuery("select * from sqlite_master where type = ?", new String[]{"table"});
                if (cursor.moveToFirst()) {
                    do {
                        String tableName = cursor.getString(cursor.getColumnIndexOrThrow("tbl_name"));
                        if (!tableNames.contains(tableName)) {
                            tableNames.add(tableName);
                        }
                    } while (cursor.moveToNext());
                }
                return tableNames;
            } catch (Exception e) {
                e.printStackTrace();
                throw new DatabaseGenerateException(e.getMessage());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static TableModel findPragmaTableInfo(String tableName, SQLiteDatabase db) {
        String defaultValue;
        if (isTableExists(tableName, db)) {
            List<String> uniqueColumns = findUniqueColumns(tableName, db);
            TableModel tableModelDB = new TableModel();
            tableModelDB.setTableName(tableName);
            String checkingColumnSQL = "pragma table_info(" + tableName + ")";
            Cursor cursor = null;
            try {
                try {
                    cursor = db.rawQuery(checkingColumnSQL, null);
                    if (cursor.moveToFirst()) {
                        do {
                            ColumnModel columnModel = new ColumnModel();
                            String name = cursor.getString(cursor.getColumnIndexOrThrow(Const.TableSchema.COLUMN_NAME));
                            String type = cursor.getString(cursor.getColumnIndexOrThrow(Const.TableSchema.COLUMN_TYPE));
                            boolean nullable = cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) != 1;
                            boolean unique = uniqueColumns.contains(name);
                            String defaultValue2 = cursor.getString(cursor.getColumnIndexOrThrow("dflt_value"));
                            columnModel.setColumnName(name);
                            columnModel.setColumnType(type);
                            columnModel.setIsNullable(nullable);
                            columnModel.setIsUnique(unique);
                            if (defaultValue2 != null) {
                                defaultValue = defaultValue2.replace("'", "");
                            } else {
                                defaultValue = "";
                            }
                            columnModel.setDefaultValue(defaultValue);
                            tableModelDB.addColumnModel(columnModel);
                        } while (cursor.moveToNext());
                    }
                    return tableModelDB;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new DatabaseGenerateException(e.getMessage());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        throw new DatabaseGenerateException(DatabaseGenerateException.TABLE_DOES_NOT_EXIST_WHEN_EXECUTING + tableName);
    }

    public static List<String> findUniqueColumns(String tableName, SQLiteDatabase db) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        Cursor innerCursor = null;
        try {
            try {
                cursor = db.rawQuery("pragma index_list(" + tableName + ")", null);
                if (cursor.moveToFirst()) {
                    do {
                        int unique = cursor.getInt(cursor.getColumnIndexOrThrow("unique"));
                        if (unique == 1) {
                            String name = cursor.getString(cursor.getColumnIndexOrThrow(Const.TableSchema.COLUMN_NAME));
                            innerCursor = db.rawQuery("pragma index_info(" + name + ")", null);
                            if (innerCursor.moveToFirst()) {
                                String columnName = innerCursor.getString(innerCursor.getColumnIndexOrThrow(Const.TableSchema.COLUMN_NAME));
                                columns.add(columnName);
                            }
                        }
                    } while (cursor.moveToNext());
                }
                return columns;
            } catch (Exception e) {
                e.printStackTrace();
                throw new DatabaseGenerateException(e.getMessage());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (innerCursor != null) {
                innerCursor.close();
            }
        }
    }
}
