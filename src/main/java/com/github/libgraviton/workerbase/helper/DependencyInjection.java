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

import java.io.*;
import java.util.*;

public class DependencyInjection {

    private static Injector injector;

    public static final String scanClass = "com.github.libgraviton";
    public static final String cacheFileName = "graviton-di-class-scan-cache";

    private static final Logger LOG = LoggerFactory.getLogger(DependencyInjection.class);

    private static final HashMap<Class<?>, Object> instanceOverrides = new HashMap<>();
    private static Map<String, List<String>> classScanCache = null;

    public static void init() {
        init(List.of());
    }

    private static Set<Class<?>> doClassScan(Class<?> interestedAnnotation) {
        return doClassScan(interestedAnnotation, true);
    }

    private static Set<Class<?>> doClassScan(Class<?> interestedAnnotation, boolean useCache) {
        final Set<Class<?>> clazzez = new HashSet<>();

        // property can override the use of the cache (disabled for tests)
        if (useCache && WorkerProperties.DI_CLASS_SCAN_USE_CACHE.get().equals("true")) {
            loadClassScanCache();
            if (classScanCache != null && classScanCache.containsKey(interestedAnnotation.getName())) {
                for (String clazzName : classScanCache.get(interestedAnnotation.getName())) {
                    try {
                        clazzez.add(DependencyInjection.class.getClassLoader().loadClass(clazzName));
                    } catch (Throwable t) {
                        LOG.error("Unable to load cached class named '{}'", clazzName, t);
                    }
                }

                return clazzez;
            }
        }

        ClassGraph classScan = new ClassGraph()
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

    public static void cacheAllClassScanResults(String path) throws IOException {
        Map<String, List<String>> classNames = new HashMap<>();

        for (Class<?> clazz : List.of(GravitonWorker.class, GravitonWorkerDiScan.class)) {
            classNames.putIfAbsent(
                    clazz.getName(),
                    doClassScan(clazz, false).stream().map(Class::getName).toList()
            );
        }

        FileOutputStream file = new FileOutputStream(path + "/" + cacheFileName);
        ObjectOutputStream out = new ObjectOutputStream(file);

        out.writeObject(classNames);

        out.close();
        file.close();
    }

    private static void loadClassScanCache() {
        // already loaded?
        if (classScanCache != null) {
            return;
        }

        try {
            InputStream is = DependencyInjection.class.getClassLoader().getResourceAsStream(cacheFileName);

            if (is == null) {
                return;
            }

            ObjectInputStream in = new ObjectInputStream(is);
            classScanCache = (Map) in.readObject();
            in.close();

            LOG.info("Successfully loaded DI class scan cache from '{}'", cacheFileName);
        } catch (Throwable t) {
            // it's ok!
        }
    }

    public static Set<Class<?>> getWorkerClasses() {
        return doClassScan(GravitonWorker.class);
    }

    public static void addInstanceOverride(Class<?> clazz, Object instance) {
        instanceOverrides.put(clazz, instance);
    }

    public static void clearInstanceOverrides() {
        instanceOverrides.clear();
    }
}
