package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Bean;
import com.ll.framework.ioc.annotations.Repository;
import com.ll.framework.ioc.annotations.Service;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApplicationContext {
    private final String basePackage;
    private final Map<String, Class<?>> providers = new HashMap<>();
    private final Map<String, Method> methods = new HashMap<>();
    private final Map<String, Object> beans = new HashMap<>();

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(basePackage)
                        .addScanners(Scanners.MethodsAnnotated)
        );
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Service.class)) {
            String name = Introspector.decapitalize(clazz.getSimpleName());
            System.out.println(name);
            providers.put(name, clazz);
        }
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Repository.class)) {
            String name = Introspector.decapitalize(clazz.getSimpleName());
            System.out.println(name);
            providers.put(name, clazz);
        }
        System.out.println("checkPoint before method bean");
        Set<Method> beanMethods = reflections.get(Scanners.MethodsAnnotated.with(Bean.class)
                .as(Method.class));
        for (Method method : beanMethods) {
            String name = Introspector.decapitalize(method.getName());
            System.out.println(name);
            methods.put(name, method);
        }
        System.out.println("checkPoint after method bean");
    }

    public <T> T genBean(String beanName) {
        Object bean = beans.get(beanName);
        if (bean != null) return (T) bean;
        Class<?> clazz = providers.get(beanName);
        if (clazz == null) return null;
        Method method = methods.get(beanName);
        if (method == null) return null;

        try {
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);

            Object[] args = new Object[constructor.getParameterTypes().length];
            for (int i = 0; i < args.length; i++) {
                String name = Introspector.decapitalize(constructor.getParameterTypes()[i].getSimpleName());
                args[i] = genBean(name);
            }
            Object instance = constructor.newInstance(args);
            beans.put(beanName, instance);
            return (T) instance;
        } catch (Exception e) {return null;}
    }


}
