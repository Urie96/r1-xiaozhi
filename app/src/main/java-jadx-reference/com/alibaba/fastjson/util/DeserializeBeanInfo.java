package com.alibaba.fastjson.util;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class DeserializeBeanInfo {
    private Constructor<?> creatorConstructor;
    private Constructor<?> defaultConstructor;
    private Method factoryMethod;
    private int parserFeatures;
    private final List<FieldInfo> fieldList = new ArrayList();
    private final List<FieldInfo> sortedFieldList = new ArrayList();

    public DeserializeBeanInfo(Class<?> clazz) {
        this.parserFeatures = 0;
        this.parserFeatures = TypeUtils.getParserFeatures(clazz);
    }

    public Constructor<?> getDefaultConstructor() {
        return this.defaultConstructor;
    }

    public void setDefaultConstructor(Constructor<?> defaultConstructor) {
        this.defaultConstructor = defaultConstructor;
    }

    public Constructor<?> getCreatorConstructor() {
        return this.creatorConstructor;
    }

    public void setCreatorConstructor(Constructor<?> createConstructor) {
        this.creatorConstructor = createConstructor;
    }

    public Method getFactoryMethod() {
        return this.factoryMethod;
    }

    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    public List<FieldInfo> getFieldList() {
        return this.fieldList;
    }

    public List<FieldInfo> getSortedFieldList() {
        return this.sortedFieldList;
    }

    public boolean add(FieldInfo field) {
        for (FieldInfo item : this.fieldList) {
            if (item.getName().equals(field.getName()) && (!item.isGetOnly() || field.isGetOnly())) {
                return false;
            }
        }
        this.fieldList.add(field);
        this.sortedFieldList.add(field);
        Collections.sort(this.sortedFieldList);
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:67:0x01dd A[PHI: r8 r9
      0x01dd: PHI (r8v6 'ordinal' int) = (r8v5 'ordinal' int), (r8v9 'ordinal' int) binds: [B:61:0x01a3, B:65:0x01bf] A[DONT_GENERATE, DONT_INLINE]
      0x01dd: PHI (r9v6 'serialzeFeatures' int) = (r9v5 'serialzeFeatures' int), (r9v9 'serialzeFeatures' int) binds: [B:61:0x01a3, B:65:0x01bf] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Removed duplicated region for block: B:97:0x02e2 A[PHI: r8 r9
      0x02e2: PHI (r8v7 'ordinal' int) = (r8v6 'ordinal' int), (r8v6 'ordinal' int), (r8v8 'ordinal' int) binds: [B:79:0x0243, B:81:0x024d, B:83:0x0263] A[DONT_GENERATE, DONT_INLINE]
      0x02e2: PHI (r9v7 'serialzeFeatures' int) = (r9v6 'serialzeFeatures' int), (r9v6 'serialzeFeatures' int), (r9v8 'serialzeFeatures' int) binds: [B:79:0x0243, B:81:0x024d, B:83:0x0263] A[DONT_GENERATE, DONT_INLINE]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static DeserializeBeanInfo computeSetters(Class<?> clazz, Type type) {
        String propertyName;
        String propertyName2;
        JSONField fieldAnnotation;
        DeserializeBeanInfo beanInfo = new DeserializeBeanInfo(clazz);
        Constructor<?> defaultConstructor = getDefaultConstructor(clazz);
        if (defaultConstructor != null) {
            TypeUtils.setAccessible(defaultConstructor);
            beanInfo.setDefaultConstructor(defaultConstructor);
        } else {
            if (defaultConstructor == null && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                Constructor<?> creatorConstructor = getCreatorConstructor(clazz);
                if (creatorConstructor != null) {
                    TypeUtils.setAccessible(creatorConstructor);
                    beanInfo.setCreatorConstructor(creatorConstructor);
                    for (int i = 0; i < creatorConstructor.getParameterTypes().length; i++) {
                        Annotation[] paramAnnotations = creatorConstructor.getParameterAnnotations()[i];
                        JSONField fieldAnnotation2 = null;
                        int len$ = paramAnnotations.length;
                        int i$ = 0;
                        while (true) {
                            if (i$ >= len$) {
                                break;
                            }
                            Annotation paramAnnotation = paramAnnotations[i$];
                            if (!(paramAnnotation instanceof JSONField)) {
                                i$++;
                            } else {
                                fieldAnnotation2 = (JSONField) paramAnnotation;
                                break;
                            }
                        }
                        if (fieldAnnotation2 == null) {
                            throw new JSONException("illegal json creator");
                        }
                        Class<?> fieldClass = creatorConstructor.getParameterTypes()[i];
                        Type fieldType = creatorConstructor.getGenericParameterTypes()[i];
                        Field field = TypeUtils.getField(clazz, fieldAnnotation2.name());
                        int ordinal = fieldAnnotation2.ordinal();
                        int serialzeFeatures = SerializerFeature.of(fieldAnnotation2.serialzeFeatures());
                        FieldInfo fieldInfo = new FieldInfo(fieldAnnotation2.name(), clazz, fieldClass, fieldType, field, ordinal, serialzeFeatures);
                        beanInfo.add(fieldInfo);
                    }
                } else {
                    Method factoryMethod = getFactoryMethod(clazz);
                    if (factoryMethod != null) {
                        TypeUtils.setAccessible(factoryMethod);
                        beanInfo.setFactoryMethod(factoryMethod);
                        for (int i2 = 0; i2 < factoryMethod.getParameterTypes().length; i2++) {
                            Annotation[] paramAnnotations2 = factoryMethod.getParameterAnnotations()[i2];
                            JSONField fieldAnnotation3 = null;
                            int len$2 = paramAnnotations2.length;
                            int i$2 = 0;
                            while (true) {
                                if (i$2 >= len$2) {
                                    break;
                                }
                                Annotation paramAnnotation2 = paramAnnotations2[i$2];
                                if (!(paramAnnotation2 instanceof JSONField)) {
                                    i$2++;
                                } else {
                                    fieldAnnotation3 = (JSONField) paramAnnotation2;
                                    break;
                                }
                            }
                            if (fieldAnnotation3 == null) {
                                throw new JSONException("illegal json creator");
                            }
                            Class<?> fieldClass2 = factoryMethod.getParameterTypes()[i2];
                            Type fieldType2 = factoryMethod.getGenericParameterTypes()[i2];
                            Field field2 = TypeUtils.getField(clazz, fieldAnnotation3.name());
                            int ordinal2 = fieldAnnotation3.ordinal();
                            int serialzeFeatures2 = SerializerFeature.of(fieldAnnotation3.serialzeFeatures());
                            FieldInfo fieldInfo2 = new FieldInfo(fieldAnnotation3.name(), clazz, fieldClass2, fieldType2, field2, ordinal2, serialzeFeatures2);
                            beanInfo.add(fieldInfo2);
                        }
                    } else {
                        throw new JSONException("default constructor not found. " + clazz);
                    }
                }
            }
            return beanInfo;
        }
        Method[] arr$ = clazz.getMethods();
        for (Method method : arr$) {
            int ordinal3 = 0;
            int serialzeFeatures3 = 0;
            String methodName = method.getName();
            if (methodName.length() >= 4 && !Modifier.isStatic(method.getModifiers()) && ((method.getReturnType().equals(Void.TYPE) || method.getReturnType().equals(clazz)) && method.getParameterTypes().length == 1)) {
                JSONField annotation = (JSONField) method.getAnnotation(JSONField.class);
                if (annotation == null) {
                    annotation = TypeUtils.getSupperMethodAnnotation(clazz, method);
                }
                if (annotation != null) {
                    if (annotation.deserialize()) {
                        ordinal3 = annotation.ordinal();
                        serialzeFeatures3 = SerializerFeature.of(annotation.serialzeFeatures());
                        if (annotation.name().length() != 0) {
                            String propertyName3 = annotation.name();
                            beanInfo.add(new FieldInfo(propertyName3, method, (Field) null, clazz, type, ordinal3, serialzeFeatures3));
                            TypeUtils.setAccessible(method);
                        }
                    }
                } else if (methodName.startsWith("set")) {
                    char c3 = methodName.charAt(3);
                    if (Character.isUpperCase(c3)) {
                        if (TypeUtils.compatibleWithJavaBean) {
                            propertyName2 = TypeUtils.decapitalize(methodName.substring(3));
                        } else {
                            propertyName2 = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                        }
                    } else if (c3 == '_') {
                        propertyName2 = methodName.substring(4);
                    } else if (c3 == 'f') {
                        propertyName2 = methodName.substring(3);
                    } else if (methodName.length() >= 5 && Character.isUpperCase(methodName.charAt(4))) {
                        propertyName2 = TypeUtils.decapitalize(methodName.substring(3));
                    }
                    Field field3 = TypeUtils.getField(clazz, propertyName2);
                    if (field3 == null && method.getParameterTypes()[0] == Boolean.TYPE) {
                        String isFieldName = "is" + Character.toUpperCase(propertyName2.charAt(0)) + propertyName2.substring(1);
                        field3 = TypeUtils.getField(clazz, isFieldName);
                    }
                    if (field3 != null && (fieldAnnotation = (JSONField) field3.getAnnotation(JSONField.class)) != null) {
                        ordinal3 = fieldAnnotation.ordinal();
                        serialzeFeatures3 = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                        if (fieldAnnotation.name().length() != 0) {
                            String propertyName4 = fieldAnnotation.name();
                            beanInfo.add(new FieldInfo(propertyName4, method, field3, clazz, type, ordinal3, serialzeFeatures3));
                        }
                    } else {
                        beanInfo.add(new FieldInfo(propertyName2, method, (Field) null, clazz, type, ordinal3, serialzeFeatures3));
                        TypeUtils.setAccessible(method);
                    }
                }
            }
        }
        Field[] arr$2 = clazz.getFields();
        int len$3 = arr$2.length;
        int i$3 = 0;
        while (true) {
            int i$4 = i$3;
            if (i$4 >= len$3) {
                break;
            }
            Field field4 = arr$2[i$4];
            if (!Modifier.isStatic(field4.getModifiers())) {
                boolean contains = false;
                for (FieldInfo item : beanInfo.getFieldList()) {
                    if (item.getName().equals(field4.getName())) {
                        contains = true;
                    }
                }
                if (!contains) {
                    int ordinal4 = 0;
                    int serialzeFeatures4 = 0;
                    String propertyName5 = field4.getName();
                    JSONField fieldAnnotation4 = (JSONField) field4.getAnnotation(JSONField.class);
                    if (fieldAnnotation4 != null) {
                        ordinal4 = fieldAnnotation4.ordinal();
                        serialzeFeatures4 = SerializerFeature.of(fieldAnnotation4.serialzeFeatures());
                        if (fieldAnnotation4.name().length() != 0) {
                            propertyName5 = fieldAnnotation4.name();
                        }
                    }
                    beanInfo.add(new FieldInfo(propertyName5, (Method) null, field4, clazz, type, ordinal4, serialzeFeatures4));
                }
            }
            i$3 = i$4 + 1;
        }
        Method[] arr$3 = clazz.getMethods();
        for (Method method2 : arr$3) {
            String methodName2 = method2.getName();
            if (methodName2.length() >= 4 && !Modifier.isStatic(method2.getModifiers()) && methodName2.startsWith("get") && Character.isUpperCase(methodName2.charAt(3)) && method2.getParameterTypes().length == 0 && (Collection.class.isAssignableFrom(method2.getReturnType()) || Map.class.isAssignableFrom(method2.getReturnType()))) {
                JSONField annotation2 = (JSONField) method2.getAnnotation(JSONField.class);
                if (annotation2 != null && annotation2.name().length() > 0) {
                    propertyName = annotation2.name();
                } else {
                    propertyName = Character.toLowerCase(methodName2.charAt(3)) + methodName2.substring(4);
                }
                beanInfo.add(new FieldInfo(propertyName, method2, (Field) null, clazz, type));
                TypeUtils.setAccessible(method2);
            }
        }
        return beanInfo;
    }

    public static Constructor<?> getDefaultConstructor(Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        Constructor<?> defaultConstructor = null;
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        int len$ = declaredConstructors.length;
        int i$ = 0;
        while (true) {
            if (i$ >= len$) {
                break;
            }
            Constructor<?> constructor = declaredConstructors[i$];
            if (constructor.getParameterTypes().length != 0) {
                i$++;
            } else {
                defaultConstructor = constructor;
                break;
            }
        }
        if (defaultConstructor == null && clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            for (Constructor<?> constructor2 : clazz.getDeclaredConstructors()) {
                if (constructor2.getParameterTypes().length == 1 && constructor2.getParameterTypes()[0].equals(clazz.getDeclaringClass())) {
                    return constructor2;
                }
            }
            return defaultConstructor;
        }
        return defaultConstructor;
    }

    public static Constructor<?> getCreatorConstructor(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            JSONCreator annotation = (JSONCreator) constructor.getAnnotation(JSONCreator.class);
            if (annotation != null) {
                if (0 != 0) {
                    throw new JSONException("multi-json creator");
                }
                return constructor;
            }
        }
        return null;
    }

    public static Method getFactoryMethod(Class<?> clazz) {
        Method[] arr$ = clazz.getDeclaredMethods();
        for (Method method : arr$) {
            if (Modifier.isStatic(method.getModifiers()) && clazz.isAssignableFrom(method.getReturnType())) {
                JSONCreator annotation = (JSONCreator) method.getAnnotation(JSONCreator.class);
                if (annotation != null) {
                    if (0 != 0) {
                        throw new JSONException("multi-json creator");
                    }
                    return method;
                }
            }
        }
        return null;
    }

    public int getParserFeatures() {
        return this.parserFeatures;
    }
}
