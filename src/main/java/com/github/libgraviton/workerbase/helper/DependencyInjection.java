package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.DummyWorker;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.di.WorkerBaseProvider;
import com.google.common.reflect.ClassPath;
import io.activej.inject.Injector;
import io.activej.inject.Key;
import io.activej.inject.binding.Binding;
import io.activej.inject.binding.BindingGenerator;
import io.activej.inject.module.ModuleBuilder;
import io.activej.inject.util.Constructors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                // worker annotation?
                GravitonWorker workerAnnotation = clazz.getAnnotation(GravitonWorker.class);
                if (workerAnnotation != null) {
                    // create a binding for a worker!
                    Constructor<?> cons = clazz.getConstructor(WorkerScope.class);

                    Method theMethod = null;
                    for (Method oneMethod : clazz.getMethods()) {
                        theMethod = oneMethod;
                    }

                    (Constructors.Constructor1) theMethod;

                    /*
                    Constructors.Constructor1<Object, Object> cons2 = new Constructors.Constructor1<>() {
                        @Override
                        public @NotNull Object create(Object arg1) {
                            return WorkerScope.class;
                        }
                    };

                     */

                    Constructors.Constructor1<WorkerScope, DummyWorker> hans = DummyWorker::new;

                    //builder.bind(clazz).to(cons, Key.of(WorkerScope.class));

                    //builder.bind(clazz).to.to(cons2, Key.of(WorkerScope.class));
                    
                    /*
                    builder.install(
                            ModuleBuilder.create().bindInstanceInjector(clazz).build()
                    );
                    
                     */
                    continue;

                    //Key.of(clazz), Binding.to(clazz::new, Pastry.class)

                }

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
