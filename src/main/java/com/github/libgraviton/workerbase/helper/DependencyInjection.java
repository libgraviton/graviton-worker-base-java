package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.WorkerInterface;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.di.WorkerBaseProvider;
import io.activej.inject.Injector;
import io.activej.inject.module.ModuleBuilder;
import io.github.classgraph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class DependencyInjection {

    private static Injector injector;

    public static final String scanClass = "com.github.libgraviton";

    private static final Logger LOG = LoggerFactory.getLogger(DependencyInjection.class);

    private static final HashMap<Class<?>, Object> instanceOverrides = new HashMap<>();

    public static void init() {
        init(List.of());
    }

    private static Set<Class<?>> doClassScan(Class<?> interestedAnnotation) {
        final Set<Class<?>> clazzez = new HashSet<>();

        ClassGraph classScan = new ClassGraph()
                .acceptClasses()
                .enableAnnotationInfo()
                .acceptPackages(scanClass);

        try (ScanResult scanResult = classScan.scan()) {
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(interestedAnnotation.getName())) {
                clazzez.add(classInfo.loadClass());
            }
        }

        return clazzez;
    }

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

            for (Class<?> clazz : doClassScan(GravitonWorker.class)) {
                final Class<WorkerInterface> workerClazz = (Class<WorkerInterface>) clazz.asSubclass(WorkerInterface.class);

                builder.bind(workerClazz).to(workerScope -> {
                    try {
                        return workerClazz
                                .getConstructor(WorkerScope.class)
                                .newInstance(workerScope);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }, WorkerScope.class);

                try {
                    builder.scan(clazz);
                } catch (Throwable t) {
                    throw new RuntimeException("DI scan error on class '"+clazz.getName()+"' at index '"+classCounter+"'", t);
                }
                classCounter++;
            }

            for (Class<?> clazz : doClassScan(GravitonWorkerDiScan.class)) {
                try {
                    builder.scan(clazz);
                } catch (Throwable t) {
                    throw new RuntimeException("DI scan error on class '"+clazz.getName()+"' at index '"+classCounter+"'", t);
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

    public static <T> T getInstance(Class<T> clazz) {
        if (injector == null) {
            init();
        }
        if (instanceOverrides.containsKey(clazz)) {
            return (T) instanceOverrides.get(clazz);
        }
        return injector.getInstance(clazz);
    }

    public static Set<Class<?>> getWorkerClasses() throws IOException {
        return doClassScan(GravitonWorker.class);
    }

    public static void addInstanceOverride(Class<?> clazz, Object instance) {
        instanceOverrides.put(clazz, instance);
    }

    public static void clearInstanceOverrides() {
        instanceOverrides.clear();
    }
}
