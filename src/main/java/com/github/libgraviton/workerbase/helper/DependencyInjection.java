package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.WorkerInterface;
import com.github.libgraviton.workerbase.di.WorkerBaseProvider;
import io.activej.inject.Injector;
import io.activej.inject.module.Module;
import io.activej.inject.module.ModuleBuilder;

import java.util.List;

public class DependencyInjection {

    private static Injector injector;

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

    public static Injector getInjector() {
        return injector;
    }

    public static void reconfigureWithModule(Module module) {
        injector = Injector.of(injector, module);
    }
}
