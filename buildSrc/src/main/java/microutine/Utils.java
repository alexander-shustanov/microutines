package microutine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Utils {
    private static Field methodSignature;

    static {
        Class<Method> methodClass = Method.class;
        try {
            methodSignature = methodClass.getDeclaredField("signature");
            methodSignature.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSignature(Method runMethod) {
        try {
            return (String) methodSignature.get(runMethod);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isSuspendPoint(ClassLoader classLoader, String owner, String methodName) {
        owner = owner.replace('/', '.');
        try {
            Class<?> aClass = classLoader.loadClass(owner);
            for (Method method : aClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    if (isSuspendMethod(classLoader, method)) return true;
                }
            }
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    if (isSuspendMethod(classLoader, method)) return true;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isSuspendMethod(ClassLoader classLoader, Method method) throws ClassNotFoundException {
        //noinspection unchecked
        return method.isAnnotationPresent((Class<? extends Annotation>) classLoader.loadClass("microutine.core.Suspend"));
    }
}
