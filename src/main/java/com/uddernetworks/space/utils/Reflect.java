package com.uddernetworks.space.utils;

import org.bukkit.Bukkit;

import java.lang.reflect.*;

public class Reflect {

    public static Class<?> getNMSClass(String nmsClassString) {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class<?> getCraftBukkitClass(String craftbukkitClassString) {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version + craftbukkitClassString;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getField(Object instance, String fieldName, boolean isPublic) {
        return getField(instance, instance.getClass(), fieldName, isPublic);
    }

    public static Object getField(Object instance, Class<?> clazz, String fieldName, boolean isPublic) {
        try {
            Field field = isPublic ? clazz.getField(fieldName) : clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setField(Object instance, String fieldName, Object fieldValue, boolean isPublic) {
        setField(instance, instance.getClass(), fieldName, fieldValue, isPublic);
    }

    public static void setField(Object instance, String fieldName, Object fieldValue, boolean isPublic, boolean isFinal) {
        setField(instance, instance.getClass(), fieldName, fieldValue, isPublic, isFinal);
    }

    public static void setField(Object instance, Class<?> clazz, String fieldName, Object fieldValue, boolean isPublic) {
        setField(instance, clazz, fieldName, fieldValue, isPublic, false);
    }

    public static void setField(Object instance, Class<?> clazz, String fieldName, Object fieldValue, boolean isPublic, boolean isFinal) {
        try {
            Field field = isPublic ? clazz.getField(fieldName) : clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            if (isFinal) {
                Field modifiersField2 = Field.class.getDeclaredField("modifiers");
                modifiersField2.setAccessible(true);
                modifiersField2.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }

            field.set(instance, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object invokeMethod(Object instance, String methodName, Class<?>[] parameterTypes, Object[] args, boolean isPublic) {
        return invokeMethod(instance, instance.getClass(), methodName, parameterTypes, args, isPublic);
    }

    public static Object invokeMethod(Object instance, Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object[] args, boolean isPublic) {
        try {
            if (parameterTypes == null || parameterTypes.length == 0) {
                return invokeMethod(instance, clazz, methodName, isPublic);
            } else {
                Method method = isPublic ? clazz.getMethod(methodName, parameterTypes) : clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method.invoke(instance, args);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object invokeMethod(Object instance, String methodName, boolean isPublic) {
        return invokeMethod(instance, instance.getClass(), methodName, isPublic);
    }

    public static Object invokeMethod(Object instance, Class<?> clazz, String methodName, boolean isPublic) {
        try {
            Method method = isPublic ? clazz.getMethod(methodName) : clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(instance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object newInstance(Class<?> clazz, Class<?>[] parameterTypes, Object[] args, boolean isPublic) {
        try {
            Constructor constructor = isPublic ? clazz.getConstructor(parameterTypes) : clazz.getDeclaredConstructor(parameterTypes);
            return constructor.newInstance(args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object castTo(Object from, Class<?> toClass) {
        return toClass.cast(from);
    }

}
