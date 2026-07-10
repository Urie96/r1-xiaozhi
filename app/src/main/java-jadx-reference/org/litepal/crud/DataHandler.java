package org.litepal.crud;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;
import com.google.android.exoplayer2.text.ttml.TtmlNode;
import com.unisound.vui.priority.PriorityMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.litepal.LitePalBase;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.DataSupportException;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;

/* JADX INFO: loaded from: classes.dex */
abstract class DataHandler extends LitePalBase {
    public static final String TAG = "DataHandler";
    private List<AssociationsInfo> fkInCurrentModel;
    private List<AssociationsInfo> fkInOtherModel;
    SQLiteDatabase mDatabase;
    private DataSupport tempEmptyModel;

    DataHandler() {
    }

    /* JADX WARN: Removed duplicated region for block: B:22:0x008e  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected <T> List<T> query(Class<T> modelClass, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, List<AssociationsInfo> foreignKeyAssociations) throws Throwable {
        Cursor cursor;
        ArrayList arrayList = new ArrayList();
        try {
            List<Field> supportedFields = getSupportedFields(modelClass.getName());
            String tableName = getTableName(modelClass);
            String[] customizedColumns = getCustomizedColumns(columns, foreignKeyAssociations);
            cursor = this.mDatabase.query(tableName, customizedColumns, selection, selectionArgs, groupBy, having, orderBy, limit);
            try {
                try {
                    if (cursor.moveToFirst()) {
                        SparseArray<QueryInfoCache> queryInfoCacheSparseArray = new SparseArray<>();
                        do {
                            Object objCreateInstanceFromClass = createInstanceFromClass(modelClass);
                            giveBaseObjIdValue((DataSupport) objCreateInstanceFromClass, cursor.getLong(cursor.getColumnIndexOrThrow(TtmlNode.ATTR_ID)));
                            setValueToModel(objCreateInstanceFromClass, supportedFields, foreignKeyAssociations, cursor, queryInfoCacheSparseArray);
                            if (foreignKeyAssociations != null) {
                                setAssociatedModel((DataSupport) objCreateInstanceFromClass);
                            }
                            arrayList.add(objCreateInstanceFromClass);
                        } while (cursor.moveToNext());
                        queryInfoCacheSparseArray.clear();
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    return arrayList;
                } catch (Exception e) {
                    e = e;
                    e.printStackTrace();
                    throw new DataSupportException(e.getMessage());
                }
            } catch (Throwable th) {
                th = th;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (Exception e2) {
            e = e2;
            cursor = null;
        } catch (Throwable th2) {
            th = th2;
            cursor = null;
            if (cursor != null) {
            }
            throw th;
        }
    }

    protected <T> T mathQuery(String str, String[] strArr, String[] strArr2, Class<T> cls) {
        BaseUtility.checkConditionsCorrect(strArr2);
        Cursor cursorQuery = null;
        T t = (T) null;
        try {
            try {
                cursorQuery = this.mDatabase.query(str, strArr, getWhereClause(strArr2), getWhereArgs(strArr2), null, null, null);
                if (cursorQuery.moveToFirst()) {
                    t = (T) cursorQuery.getClass().getMethod(genGetColumnMethod((Class<?>) cls), Integer.TYPE).invoke(cursorQuery, 0);
                }
                return t;
            } catch (Exception e) {
                throw new DataSupportException(e.getMessage());
            }
        } finally {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        }
    }

    protected void giveBaseObjIdValue(DataSupport baseObj, long id) throws IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException {
        if (id > 0) {
            DynamicExecutor.setField(baseObj, "baseObjId", Long.valueOf(id), DataSupport.class);
        }
    }

    protected void putFieldsValue(DataSupport baseObj, List<Field> supportedFields, ContentValues values) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        for (Field field : supportedFields) {
            if (!isIdColumn(field.getName())) {
                putFieldsValueDependsOnSaveOrUpdate(baseObj, field, values);
            }
        }
    }

    protected void putContentValues(DataSupport baseObj, Field field, ContentValues values) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        Object fieldValue = takeGetMethodValueByField(baseObj, field);
        if ("java.util.Date".equals(field.getType().getName()) && fieldValue != null) {
            Date date = (Date) fieldValue;
            fieldValue = Long.valueOf(date.getTime());
        }
        Object[] parameters = {BaseUtility.changeCase(field.getName()), fieldValue};
        DynamicExecutor.send(values, "put", parameters, values.getClass(), getParameterTypes(field, fieldValue, parameters));
    }

    protected Object takeGetMethodValueByField(DataSupport dataSupport, Field field) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (!shouldGetOrSet(dataSupport, field)) {
            return null;
        }
        String getMethodName = makeGetterMethodName(field);
        return DynamicExecutor.send(dataSupport, getMethodName, null, dataSupport.getClass(), null);
    }

    protected void putSetMethodValueByField(DataSupport dataSupport, Field field, Object parameter) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (shouldGetOrSet(dataSupport, field)) {
            String setMethodName = makeSetterMethodName(field);
            DynamicExecutor.send(dataSupport, setMethodName, new Object[]{parameter}, dataSupport.getClass(), new Class[]{field.getType()});
        }
    }

    protected void analyzeAssociatedModels(DataSupport baseObj, Collection<AssociationsInfo> associationInfos) {
        try {
            for (AssociationsInfo associationInfo : associationInfos) {
                if (associationInfo.getAssociationType() == 2) {
                    new Many2OneAnalyzer().analyze(baseObj, associationInfo);
                } else if (associationInfo.getAssociationType() == 1) {
                    new One2OneAnalyzer().analyze(baseObj, associationInfo);
                } else if (associationInfo.getAssociationType() == 3) {
                    new Many2ManyAnalyzer().analyze(baseObj, associationInfo);
                }
            }
        } catch (Exception e) {
            throw new DataSupportException(e.getMessage());
        }
    }

    protected DataSupport getAssociatedModel(DataSupport baseObj, AssociationsInfo associationInfo) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        return (DataSupport) takeGetMethodValueByField(baseObj, associationInfo.getAssociateOtherModelFromSelf());
    }

    protected Collection<DataSupport> getAssociatedModels(DataSupport baseObj, AssociationsInfo associationInfo) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        return (Collection) takeGetMethodValueByField(baseObj, associationInfo.getAssociateOtherModelFromSelf());
    }

    protected DataSupport getEmptyModel(DataSupport baseObj) {
        if (this.tempEmptyModel != null) {
            return this.tempEmptyModel;
        }
        String className = null;
        try {
            className = baseObj.getClassName();
            Class<?> modelClass = Class.forName(className);
            this.tempEmptyModel = (DataSupport) modelClass.newInstance();
            return this.tempEmptyModel;
        } catch (ClassNotFoundException e) {
            throw new DatabaseGenerateException(DatabaseGenerateException.CLASS_NOT_FOUND + className);
        } catch (InstantiationException e2) {
            throw new DataSupportException(String.valueOf(className) + DataSupportException.INSTANTIATION_EXCEPTION);
        } catch (Exception e3) {
            throw new DataSupportException(e3.getMessage());
        }
    }

    protected String getWhereClause(String... conditions) {
        if (isAffectAllLines(conditions) || conditions == null || conditions.length <= 0) {
            return null;
        }
        return conditions[0];
    }

    protected String[] getWhereArgs(String... conditions) {
        if (isAffectAllLines(conditions) || conditions == null || conditions.length <= 1) {
            return null;
        }
        String[] whereArgs = new String[conditions.length - 1];
        System.arraycopy(conditions, 1, whereArgs, 0, conditions.length - 1);
        return whereArgs;
    }

    protected boolean isAffectAllLines(Object... conditions) {
        return conditions != null && conditions.length == 0;
    }

    protected String getWhereOfIdsWithOr(Collection<Long> ids) {
        StringBuilder whereClause = new StringBuilder();
        boolean needOr = false;
        Iterator<Long> it = ids.iterator();
        while (it.hasNext()) {
            long id = it.next().longValue();
            if (needOr) {
                whereClause.append(" or ");
            }
            needOr = true;
            whereClause.append("id = ");
            whereClause.append(id);
        }
        return BaseUtility.changeCase(whereClause.toString());
    }

    protected String getWhereOfIdsWithOr(long... ids) {
        StringBuilder whereClause = new StringBuilder();
        boolean needOr = false;
        for (long id : ids) {
            if (needOr) {
                whereClause.append(" or ");
            }
            needOr = true;
            whereClause.append("id = ");
            whereClause.append(id);
        }
        return BaseUtility.changeCase(whereClause.toString());
    }

    @Deprecated
    protected Class<?> findDataSupportClass(DataSupport baseObj) {
        Class<?> superClass;
        do {
            superClass = baseObj.getClass().getSuperclass();
            if (superClass == null) {
                break;
            }
        } while (DataSupport.class != superClass);
        if (superClass == null) {
            throw new DataSupportException(String.valueOf(baseObj.getClass().getName()) + DataSupportException.MODEL_IS_NOT_AN_INSTANCE_OF_DATA_SUPPORT);
        }
        return superClass;
    }

    protected boolean shouldGetOrSet(DataSupport dataSupport, Field field) {
        return (dataSupport == null || field == null) ? false : true;
    }

    protected String getIntermediateTableName(DataSupport baseObj, String associatedTableName) {
        return BaseUtility.changeCase(DBUtility.getIntermediateTableName(baseObj.getTableName(), associatedTableName));
    }

    protected String getTableName(Class<?> modelClass) {
        return BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName()));
    }

    protected Object createInstanceFromClass(Class<?> modelClass) {
        try {
            Constructor<?> constructor = findBestSuitConstructor(modelClass);
            return constructor.newInstance(getConstructorParams(modelClass, constructor));
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataSupportException(e.getMessage());
        }
    }

    protected Constructor<?> findBestSuitConstructor(Class<?> modelClass) {
        Constructor<?>[] declaredConstructors = modelClass.getDeclaredConstructors();
        SparseArray<Constructor<?>> map = new SparseArray<>();
        int minKey = PriorityMap.PRIORITY_MAX;
        for (Constructor<?> constructor : declaredConstructors) {
            int key = constructor.getParameterTypes().length;
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                if (parameterType == modelClass) {
                    key += 10000;
                }
            }
            if (map.get(key) == null) {
                map.put(key, constructor);
            }
            if (key < minKey) {
                minKey = key;
            }
        }
        Constructor<?> bestSuitConstructor = map.get(minKey);
        if (bestSuitConstructor != null) {
            bestSuitConstructor.setAccessible(true);
        }
        return bestSuitConstructor;
    }

    protected Object[] getConstructorParams(Class<?> modelClass, Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            params[i] = getInitParamValue(modelClass, parameterTypes[i]);
        }
        return params;
    }

    protected void setValueToModel(Object modelInstance, List<Field> supportedFields, List<AssociationsInfo> foreignKeyAssociations, Cursor cursor, SparseArray<QueryInfoCache> sparseArray) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        int cacheSize = sparseArray.size();
        if (cacheSize > 0) {
            for (int i = 0; i < cacheSize; i++) {
                int columnIndex = sparseArray.keyAt(i);
                QueryInfoCache cache = sparseArray.get(columnIndex);
                setToModelByReflection(modelInstance, cache.field, columnIndex, cache.getMethodName, cursor);
            }
        } else {
            for (Field field : supportedFields) {
                String getMethodName = genGetColumnMethod(field);
                String columnName = isIdColumn(field.getName()) ? TtmlNode.ATTR_ID : field.getName();
                int columnIndex2 = cursor.getColumnIndex(BaseUtility.changeCase(columnName));
                if (columnIndex2 != -1) {
                    setToModelByReflection(modelInstance, field, columnIndex2, getMethodName, cursor);
                    QueryInfoCache cache2 = new QueryInfoCache();
                    cache2.getMethodName = getMethodName;
                    cache2.field = field;
                    sparseArray.put(columnIndex2, cache2);
                }
            }
        }
        if (foreignKeyAssociations != null) {
            for (AssociationsInfo associationInfo : foreignKeyAssociations) {
                String foreignKeyColumn = getForeignKeyColumnName(DBUtility.getTableNameByClassName(associationInfo.getAssociatedClassName()));
                int columnIndex3 = cursor.getColumnIndex(foreignKeyColumn);
                if (columnIndex3 != -1) {
                    long associatedClassId = cursor.getLong(columnIndex3);
                    try {
                        DataSupport associatedObj = (DataSupport) DataSupport.find(Class.forName(associationInfo.getAssociatedClassName()), associatedClassId);
                        if (associatedObj != null) {
                            putSetMethodValueByField((DataSupport) modelInstance, associationInfo.getAssociateOtherModelFromSelf(), associatedObj);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected List<AssociationsInfo> getForeignKeyAssociations(String className, boolean isEager) {
        if (!isEager) {
            return null;
        }
        analyzeAssociations(className);
        return this.fkInCurrentModel;
    }

    private Class<?>[] getParameterTypes(Field field, Object fieldValue, Object[] parameters) {
        if (isCharType(field)) {
            parameters[1] = String.valueOf(fieldValue);
            return new Class[]{String.class, String.class};
        }
        if (field.getType().isPrimitive()) {
            return new Class[]{String.class, getObjectType(field.getType())};
        }
        if ("java.util.Date".equals(field.getType().getName())) {
            return new Class[]{String.class, Long.class};
        }
        return new Class[]{String.class, field.getType()};
    }

    private Class<?> getObjectType(Class<?> primitiveType) {
        if (primitiveType != null && primitiveType.isPrimitive()) {
            String basicTypeName = primitiveType.getName();
            if ("int".equals(basicTypeName)) {
                return Integer.class;
            }
            if ("short".equals(basicTypeName)) {
                return Short.class;
            }
            if ("long".equals(basicTypeName)) {
                return Long.class;
            }
            if ("float".equals(basicTypeName)) {
                return Float.class;
            }
            if ("double".equals(basicTypeName)) {
                return Double.class;
            }
            if ("boolean".equals(basicTypeName)) {
                return Boolean.class;
            }
            if ("char".equals(basicTypeName)) {
                return Character.class;
            }
        }
        return null;
    }

    private Object getInitParamValue(Class<?> modelClass, Class<?> paramType) {
        String paramTypeName = paramType.getName();
        if ("boolean".equals(paramTypeName) || "java.lang.Boolean".equals(paramTypeName)) {
            return false;
        }
        if ("float".equals(paramTypeName) || "java.lang.Float".equals(paramTypeName)) {
            return Float.valueOf(0.0f);
        }
        if ("double".equals(paramTypeName) || "java.lang.Double".equals(paramTypeName)) {
            return Double.valueOf(0.0d);
        }
        if ("int".equals(paramTypeName) || "java.lang.Integer".equals(paramTypeName)) {
            return 0;
        }
        if ("long".equals(paramTypeName) || "java.lang.Long".equals(paramTypeName)) {
            return 0L;
        }
        if ("short".equals(paramTypeName) || "java.lang.Short".equals(paramTypeName)) {
            return 0;
        }
        if ("char".equals(paramTypeName) || "java.lang.Character".equals(paramTypeName)) {
            return ' ';
        }
        if ("[B".equals(paramTypeName) || "[Ljava.lang.Byte;".equals(paramTypeName)) {
            return new byte[0];
        }
        if ("java.lang.String".equals(paramTypeName)) {
            return "";
        }
        if (modelClass == paramType) {
            return null;
        }
        return createInstanceFromClass(paramType);
    }

    private boolean isCharType(Field field) {
        String type = field.getType().getName();
        return type.equals("char") || type.endsWith("Character");
    }

    private boolean isPrimitiveBooleanType(Field field) {
        Class<?> fieldType = field.getType();
        return "boolean".equals(fieldType.getName());
    }

    private void putFieldsValueDependsOnSaveOrUpdate(DataSupport baseObj, Field field, ContentValues values) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (isUpdating()) {
            if (!isFieldWithDefaultValue(baseObj, field)) {
                putContentValues(baseObj, field, values);
            }
        } else if (isSaving()) {
            Object value = takeGetMethodValueByField(baseObj, field);
            if (value != null) {
                putContentValues(baseObj, field, values);
            }
        }
    }

    private boolean isUpdating() {
        return UpdateHandler.class.getName().equals(getClass().getName());
    }

    private boolean isSaving() {
        return SaveHandler.class.getName().equals(getClass().getName());
    }

    private boolean isFieldWithDefaultValue(DataSupport baseObj, Field field) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        DataSupport emptyModel = getEmptyModel(baseObj);
        Object realReturn = takeGetMethodValueByField(baseObj, field);
        Object defaultReturn = takeGetMethodValueByField(emptyModel, field);
        if (realReturn == null || defaultReturn == null) {
            return realReturn == defaultReturn;
        }
        String realFieldValue = takeGetMethodValueByField(baseObj, field).toString();
        String defaultFieldValue = takeGetMethodValueByField(emptyModel, field).toString();
        return realFieldValue.equals(defaultFieldValue);
    }

    private String makeGetterMethodName(Field field) {
        String getterMethodPrefix;
        String fieldName = field.getName();
        if (isPrimitiveBooleanType(field)) {
            if (fieldName.matches("^is[A-Z]{1}.*$")) {
                fieldName = fieldName.substring(2);
            }
            getterMethodPrefix = "is";
        } else {
            getterMethodPrefix = "get";
        }
        if (fieldName.matches("^[a-z]{1}[A-Z]{1}.*")) {
            return String.valueOf(getterMethodPrefix) + fieldName;
        }
        return String.valueOf(getterMethodPrefix) + BaseUtility.capitalize(fieldName);
    }

    private String makeSetterMethodName(Field field) {
        if (isPrimitiveBooleanType(field) && field.getName().matches("^is[A-Z]{1}.*$")) {
            String setterMethodName = String.valueOf("set") + field.getName().substring(2);
            return setterMethodName;
        }
        if (field.getName().matches("^[a-z]{1}[A-Z]{1}.*")) {
            String setterMethodName2 = String.valueOf("set") + field.getName();
            return setterMethodName2;
        }
        String setterMethodName3 = String.valueOf("set") + BaseUtility.capitalize(field.getName());
        return setterMethodName3;
    }

    private String genGetColumnMethod(Field field) {
        return genGetColumnMethod(field.getType());
    }

    private String genGetColumnMethod(Class<?> fieldType) {
        String typeName;
        if (fieldType.isPrimitive()) {
            typeName = BaseUtility.capitalize(fieldType.getName());
        } else {
            typeName = fieldType.getSimpleName();
        }
        String methodName = "get" + typeName;
        if ("getBoolean".equals(methodName)) {
            return "getInt";
        }
        if ("getChar".equals(methodName) || "getCharacter".equals(methodName)) {
            return "getString";
        }
        if ("getDate".equals(methodName)) {
            return "getLong";
        }
        if ("getInteger".equals(methodName)) {
            return "getInt";
        }
        if ("getbyte[]".equalsIgnoreCase(methodName)) {
            return "getBlob";
        }
        return methodName;
    }

    private String[] getCustomizedColumns(String[] columns, List<AssociationsInfo> foreignKeyAssociations) {
        if (columns != null) {
            if (foreignKeyAssociations != null && foreignKeyAssociations.size() > 0) {
                String[] tempColumns = new String[columns.length + foreignKeyAssociations.size()];
                System.arraycopy(columns, 0, tempColumns, 0, columns.length);
                for (int i = 0; i < foreignKeyAssociations.size(); i++) {
                    String associatedTable = DBUtility.getTableNameByClassName(foreignKeyAssociations.get(i).getAssociatedClassName());
                    tempColumns[columns.length + i] = getForeignKeyColumnName(associatedTable);
                }
                columns = tempColumns;
            }
            for (int i2 = 0; i2 < columns.length; i2++) {
                String columnName = columns[i2];
                if (isIdColumn(columnName)) {
                    if ("_id".equalsIgnoreCase(columnName)) {
                        columns[i2] = BaseUtility.changeCase(TtmlNode.ATTR_ID);
                    }
                    return columns;
                }
            }
            String[] customizedColumns = new String[columns.length + 1];
            System.arraycopy(columns, 0, customizedColumns, 0, columns.length);
            customizedColumns[columns.length] = BaseUtility.changeCase(TtmlNode.ATTR_ID);
            return customizedColumns;
        }
        return null;
    }

    private void analyzeAssociations(String className) {
        Collection<AssociationsInfo> associationInfos = getAssociationInfo(className);
        if (this.fkInCurrentModel == null) {
            this.fkInCurrentModel = new ArrayList();
        } else {
            this.fkInCurrentModel.clear();
        }
        if (this.fkInOtherModel == null) {
            this.fkInOtherModel = new ArrayList();
        } else {
            this.fkInOtherModel.clear();
        }
        for (AssociationsInfo associationInfo : associationInfos) {
            if (associationInfo.getAssociationType() == 2 || associationInfo.getAssociationType() == 1) {
                if (associationInfo.getClassHoldsForeignKey().equals(className)) {
                    this.fkInCurrentModel.add(associationInfo);
                } else {
                    this.fkInOtherModel.add(associationInfo);
                }
            } else if (associationInfo.getAssociationType() == 3) {
                this.fkInOtherModel.add(associationInfo);
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:39:0x016c  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void setAssociatedModel(DataSupport baseObj) throws Throwable {
        Cursor cursor;
        if (this.fkInOtherModel != null) {
            for (AssociationsInfo info : this.fkInOtherModel) {
                String associatedClassName = info.getAssociatedClassName();
                boolean isM2M = info.getAssociationType() == 3;
                try {
                    List<Field> supportedFields = getSupportedFields(associatedClassName);
                    if (isM2M) {
                        String tableName = baseObj.getTableName();
                        String associatedTableName = DBUtility.getTableNameByClassName(associatedClassName);
                        String intermediateTableName = DBUtility.getIntermediateTableName(tableName, associatedTableName);
                        StringBuilder sql = new StringBuilder();
                        sql.append("select * from ").append(associatedTableName).append(" a inner join ").append(intermediateTableName).append(" b on a.id = b.").append(String.valueOf(associatedTableName) + "_id").append(" where b.").append(tableName).append("_id = ?");
                        cursor = DataSupport.findBySQL(BaseUtility.changeCase(sql.toString()), String.valueOf(baseObj.getBaseObjId()));
                    } else {
                        String foreignKeyColumn = getForeignKeyColumnName(DBUtility.getTableNameByClassName(info.getSelfClassName()));
                        cursor = this.mDatabase.query(BaseUtility.changeCase(DBUtility.getTableNameByClassName(associatedClassName)), null, String.valueOf(foreignKeyColumn) + "=?", new String[]{String.valueOf(baseObj.getBaseObjId())}, null, null, null, null);
                    }
                    if (cursor != null) {
                        try {
                            try {
                                if (cursor.moveToFirst()) {
                                    SparseArray<QueryInfoCache> queryInfoCacheSparseArray = new SparseArray<>();
                                    do {
                                        DataSupport modelInstance = (DataSupport) createInstanceFromClass(Class.forName(associatedClassName));
                                        giveBaseObjIdValue(modelInstance, cursor.getLong(cursor.getColumnIndexOrThrow(TtmlNode.ATTR_ID)));
                                        setValueToModel(modelInstance, supportedFields, null, cursor, queryInfoCacheSparseArray);
                                        if (info.getAssociationType() == 2 || isM2M) {
                                            Collection collection = (Collection) takeGetMethodValueByField(baseObj, info.getAssociateOtherModelFromSelf());
                                            collection.add(modelInstance);
                                        } else if (info.getAssociationType() == 1) {
                                            putSetMethodValueByField(baseObj, info.getAssociateOtherModelFromSelf(), modelInstance);
                                        }
                                    } while (cursor.moveToNext());
                                    queryInfoCacheSparseArray.clear();
                                }
                            } catch (Exception e) {
                                e = e;
                                throw new DataSupportException(e.getMessage());
                            }
                        } catch (Throwable th) {
                            th = th;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e2) {
                    e = e2;
                    cursor = null;
                } catch (Throwable th2) {
                    th = th2;
                    cursor = null;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
        }
    }

    private void setToModelByReflection(Object modelInstance, Field field, int columnIndex, String getMethodName, Cursor cursor) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> cursorClass = cursor.getClass();
        Method method = cursorClass.getMethod(getMethodName, Integer.TYPE);
        Object value = method.invoke(cursor, Integer.valueOf(columnIndex));
        if (isIdColumn(field.getName())) {
            DynamicExecutor.setField(modelInstance, field.getName(), value, modelInstance.getClass());
            return;
        }
        if (field.getType() == Boolean.TYPE || field.getType() == Boolean.class) {
            if ("0".equals(String.valueOf(value))) {
                value = false;
            } else if ("1".equals(String.valueOf(value))) {
                value = true;
            }
        } else if (field.getType() == Character.TYPE || field.getType() == Character.class) {
            value = Character.valueOf(((String) value).charAt(0));
        } else if (field.getType() == Date.class) {
            long date = ((Long) value).longValue();
            value = date <= 0 ? null : new Date(date);
        }
        putSetMethodValueByField((DataSupport) modelInstance, field, value);
    }

    class QueryInfoCache {
        Field field;
        String getMethodName;

        QueryInfoCache() {
        }
    }
}
