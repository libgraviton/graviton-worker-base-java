package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class Application {
  public static void main(String[] args) throws IOException, WorkerException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    WorkerProperties.load();
    DependencyInjection.init();

    // try to persist them
    WorkerProperties.persist();

    // first, the healthcheck
    setupHealthcheck();

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
      DependencyInjection.getInstance(Properties.class)
    );

    workerLauncher.run();
  }

  private static void setupHealthcheck() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    String healthcheckClass = System.getenv("HEALTHCHECK_CLASS");

    if (healthcheckClass == null || healthcheckClass.isEmpty()) {
      return;
    }

    int port = 9000;
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);

    Class<?> clazz = Class.forName(healthcheckClass);
    final Method method = clazz.getMethod("main", String[].class);
    method.setAccessible(true);

    final String[] arguments = {}; // Pass any command-line arguments here

    HttpHandler handler = exchange -> {
      String result = "OK";
      try {
        method.invoke(null, (Object) arguments);
      } catch (Throwable t) {
        result = "FAILED";
      }

      exchange.sendResponseHeaders(200, result.length());
      OutputStream os = exchange.getResponseBody();
      os.write(result.getBytes());
      os.close();
    };

    // Create a context for your lambda function
    server.createContext("/", handler);

    server.start();
    System.out.println("Started healthcheck server on port localhost:" + port);
  }
}