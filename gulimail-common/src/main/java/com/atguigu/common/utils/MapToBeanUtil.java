package com.atguigu.common.utils;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <strong>Title :<br>
 * </strong> <strong>Description :对象转换工具类</strong>@类注释说明写在此处@<br>
 * <strong>Create on : 2018年05月15日<br>
 * </strong>
 * <p>
 * <strong>Copyright (C) Vbill Co.,Ltd.<br>
 * </strong>
 * <p>
 *
 * @author department:技术开发部 <br>
 *         username:zhangtao <br>
 *         email: <br>
 * @version <strong>zw有限公司-运营平台</strong><br>
 *          <br>
 *          <strong>修改历史:</strong><br>
 *          修改人 修改日期 修改描述<br>
 *          -------------------------------------------<br>
 *          <br>
 *          <br>
 */
public class MapToBeanUtil {
    /**
     * map转换成实体类
     *
     * @param t   转换之后的实体类
     * @param map 集合
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T mapToBean(T t, Map map) throws Exception {
        Class clazz = t.getClass();
        //实例化类
        T entity = (T) clazz.newInstance();
        Set<String> keys = map.keySet();
        //变量map 赋值
        for (String key : keys) {
            String fieldName = key;
            //判断是sql 还是hql返回的结果
            if (key.equals(key.toUpperCase())) {
                //获取所有域变量
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getName().toUpperCase().equals(key)) {
                        fieldName = field.getName();
                    }
                    break;
                }
            }
            //设置赋值
            if (existsField(clazz, fieldName)) {
                //参数的类型  clazz.getField(fieldName)
                Class<?> paramClass = clazz.getDeclaredField(fieldName).getType();
                //拼装set方法名称
                String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                //根据名称获取方法
                Method method = clazz.getMethod(methodName, paramClass);
                //调用invoke执行赋值
                method.invoke(entity, map.get(key));
            }
        }
        return entity;
    }

    /**
     * 实体转换成map集合
     *
     * @param obj
     * @return
     */
    public static Map<String, Object> beanToMap(Object obj) {

        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 过滤class属性
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);

                    map.put(key, value);
                }

            }
        } catch (Exception e) {
            System.out.println("transBean2Map Error " + e);
        }
        return map;
    }

    private static boolean existsField(Class clz, String fieldName) {
        try {
            return clz.getDeclaredField(fieldName) != null;
        } catch (Exception e) {
        }
        return false;
    }

    public static <T> T objectToBean(Object o, Class<T> c) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        String str;
        try{
            str = objectMapper.writeValueAsString(o);
            return (T) objectMapper.readValue(str,c);
        }catch (IOException e){
            throw new Exception("实体转换异常");
        }

    }
}
