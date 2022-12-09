package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.di.WorkerBaseProvider;
import com.google.common.reflect.ClassPath;
import io.activej.inject.Injector;
import io.activej.inject.module.ModuleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class DependencyInjection {

    private static Injector injector;

    private static String scanClass = "com.github.libgraviton";

    private static final Logger LOG = LoggerFactory.getLogger(DependencyInjection.class);

    private static final HashMap<Class<?>, Object> instanceOverrides = new HashMap<>();

    public static void init(List<Object> addedProviders) {
        if (injector != null) {
            return;
        }

        ModuleBuilder builder = ModuleBuilder
                .create()
                .install(List.of(new WorkerBaseProvider()));

        LOG.info("DI init, starting class scan for all classes in '{}'", scanClass);
        int classCounter = 0;

        // scan all our classes in classpath!
        try {
            ClassPath classpath = ClassPath.from(DependencyInjection.class.getClassLoader());
            for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive(scanClass)) {
                // skip all 'gravitondyn'!
                if (classInfo.getName().contains(".gravitondyn.")) {
                    continue;
                }

                Class<?> clazz = classInfo.load();

                // does it have the annotation?
                GravitonWorkerDiScan annotation = clazz.getAnnotation(GravitonWorkerDiScan.class);
                if (annotation == null) {
                    continue;
                }

                LOG.info("Scanning {}", classInfo.getName());
                try {
                    builder.scan(clazz);
                } catch (Throwable t) {
                    throw new RuntimeException("DI scan error on class '"+classInfo.getName()+"' at index '"+classCounter+"'", t);
                }
                classCounter++;
            }
        } catch (Throwable t) {
            LOG.error("Unable to scan class path for DI components, that is not good!", t);
        }

        LOG.info("DI init finished, scanned '{}' classes.", classCounter);

        for (Object provider : addedProviders) {
            builder.scan(provider);
        }

        injector = Injector.of(builder.build());
    }

    public static void reset() {
        injector = null;
        init(List.of());
    }

    public static <T> T getInstance(Class<T> clazz) {
        if (instanceOverrides.containsKey(clazz)) {
            return (T) instanceOverrides.get(clazz);
        }
        return injector.getInstance(clazz);
    }

    public static void addInstanceOverride(Class<?> clazz, Object instance) {
        instanceOverrides.put(clazz, instance);
    }

    public static void clearInstanceOverrides() {
        instanceOverrides.clear();
    }
}
