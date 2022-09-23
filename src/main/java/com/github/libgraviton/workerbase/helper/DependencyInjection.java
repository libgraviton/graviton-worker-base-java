package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.WorkerInterface;
import com.github.libgraviton.workerbase.di.WorkerBaseProvider;
import io.activej.inject.Injector;
import io.activej.inject.module.ModuleBuilder;

import java.util.HashMap;
import java.util.List;

public class DependencyInjection {

    private static Injector injector;

    private static final HashMap<Class<?>, Object> instanceOverrides = new HashMap<>();

    public static void init(WorkerInterface worker, List<Object> addedProviders) {
        ModuleBuilder builder = ModuleBuilder
                .create()
                .scan(WorkerBaseProvider.class)
                .scan(worker);

        for (Class<?> provider : worker.getDependencyInjectionProviders()) {
            builder.scan(provider);
        }

        for (Object provider : addedProviders) {
            builder.scan(provider);
        }

        builder.bindInstanceInjector(worker.getClass());

        injector = Injector.of(builder.build());
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
