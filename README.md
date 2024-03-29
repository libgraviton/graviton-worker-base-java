# graviton-worker-base-java

[![Build status](https://github.com/libgraviton/graviton-worker-base-java/actions/workflows/maven.yml/badge.svg)](https://github.com/libgraviton/graviton-worker-base-java/actions/workflows/maven.yml)

## What is it

A base library to implement Java based Graviton RabbitMQ queue workers. 

## What does it do

This library provides two abstracts:

* `WorkerAbstract`<br />An abstract you can extend from to implement general purpose workers.
* `FileWorkerAbstract`<br />An abstract you can extend from to implement `/file` orientated workers that provide some more convenience functions that deal with the `/file` API.

It also contains some models (POJOs) that help when you deal with a Graviton backend (package `com.github.libgraviton.workerbase.model`).

When you extend from one of those abstracts, the worker base takes care of:

* connecting to the queue
* registering the worker on `/event/worker`
* deserializing the Queue message to a POJO
* calling your worker function
* status tracking (`working` / `done` / `failed`)
* Error handling. It also provides the class `WorkerException` that you can throw inside your logic function. Any exception thrown (and not catched) inside your logic function will turn the `/event/status` entry to `failed`.

## Configuration

There are some basic settings needed in order for the library to work. These are defined in the file `src/main/resources/default.properties`.
Additionally, you may want to have own properties and/or overrides of defaults in your worker implementation.

You may specify a path to an *additional* parsed properties file using the `propFile` system property (i.e. by command line).

Thus, once your worker implementing the base library is usable, you may specify an additional properties file like so:

```bash
java -DpropFile=path/to/additional.properties <yourCommand>
```

## Using the library

Due to frustrations with Maven central deployment, this library is now hosted at our own [bintray repository](https://bintray.com/libgraviton/maven/graviton-worker-base-java).

Add this to your pom.xml:

```xml
<repository>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
    <id>bintray-libgraviton-maven</id>
    <name>bintray</name>
    <url>https://dl.bintray.com/libgraviton/maven</url>
</repository>
```

You can use this library in your project by including this in your `pom.xml`:

```xml
<dependencies>
	<dependency>
		<groupId>com.github.libgraviton</groupId>
		<artifactId>worker-base</artifactId>
		<version>0.15.0</version>
	</dependency>
</dependencies>	
```

make sure that `version` points to the newest release on Maven central (see badge above).

## RabbitMQ integration
RabbitMQ is integrated as *Work Queues* (aka: Task Queues). The Howto page explains the implementation details. See (https://www.rabbitmq.com/tutorials/tutorial-two-java.html).


With this setup, we create a persistent, separate message queue for each `graviton.workerId` and can be sure each worker gets only the messages it needs.
Additionally we have round-robin load balancing for the case when there are more than one worker running with matching `graviton.workerId`.
Thanks to the `message acknowledgment` we also have a fail-over mechanism whenever a worker loses its connection to the message queue. In that case the queue will
requeue the message for another available worker.

Aside from the `graviton.workerId`, there is also the `queue.prefetchCount` that can be configured. Each running worker will take as many messages at a time as configured.
```xml
graviton.workerId=myWorkerId
queue.prefetchCount=2
```

## Cloudfoundry support

This library comes with basic Cloudfoundry `VCAP_SERVICES` support. If this environment variable is found with an `rabbitmq-3.0` element,
those connection credentials will automatically override whatever you specify via properties.

## Deploying a release

```
bash deploy.sh
```