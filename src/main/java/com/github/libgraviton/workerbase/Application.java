package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import io.sentry.Sentry;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class Application {
  public static void main(String[] args) throws IOException, WorkerException {
    Properties workerProperties = initPropertiesAndSentry();

    Set<Class<?>> workerClasses = DependencyInjection.getWorkerClasses();

    Class<WorkerInterface> classToLoad = null;

    // none?
    if (workerClasses.size() < 1) {
      throw new RuntimeException("Unable to locate a worker class. You need to create an implementation " +
        "of WorkerInterface within the '" + DependencyInjection.scanClass + "' namespace and annotate that " +
        "class with the @GravitonWorker annotation.");
    }

    // more than 1?
    if (workerClasses.size() > 1) {
      String mainClass = WorkerProperties.WORKER_MAIN_CLASS.get();

      if (mainClass == null || mainClass.equals("")) {
        String foundClasses = workerClasses.stream().map(Class::getName).collect(Collectors.joining(", "));
        throw new RuntimeException("More than one WorkerInterface implementation found. You need to set the " +
          "'worker.mainClass' property in your pom.xml! Found classes: " + foundClasses);
      }

      try {
        ClassLoader classLoader = Application.class.getClassLoader();
        classToLoad = (Class<WorkerInterface>) classLoader.loadClass(mainClass);
      } catch (Throwable t) {
        throw new RuntimeException("The specified class '" + mainClass + "' could not be loaded!", t);
      }
    }

    // perfect - just one.. pick that!
    if (workerClasses.size() == 1) {
      classToLoad = (Class<WorkerInterface>) workerClasses.stream().toList().get(0);
    }

    if (classToLoad == null) {
      throw new RuntimeException("Unable to discover any worker class and/or loading error!");
    }

    // exactly one
    final WorkerInterface worker = DependencyInjection.getInstance(classToLoad);

    final WorkerLauncher workerLauncher = new WorkerLauncher(
      worker,
      workerProperties,
      getApplicationName(workerProperties)
    );

    workerLauncher.run();
  }

  private static Properties initPropertiesAndSentry() throws IOException {
    WorkerProperties.load();
    DependencyInjection.init();

    // try to persist them
    WorkerProperties.persist();

    Properties workerProperties = DependencyInjection.getInstance(Properties.class);

    Sentry.init(options -> {
      String dsn = System.getenv("SENTRY_DSN");

      if (null != dsn && !dsn.isBlank()) {
        options.setDsn(dsn);
        options.setEnabled(true);
      }

      options.setRelease(
        String.format(
          "%s@%s",
          getApplicationName(workerProperties),
          workerProperties.getProperty("application.version")
        )
      );
    });

    return workerProperties;
  }

  private static String getApplicationName(Properties properties)
  {
    return properties.getProperty("graviton.workerName") != null ? properties.getProperty("graviton.workerName") : properties.getProperty("application.name");
  }
}