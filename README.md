# graviton-worker-base-java

[![Build Status](https://travis-ci.org/libgraviton/graviton-worker-base-java.svg?branch=develop)](https://travis-ci.org/libgraviton/graviton-worker-base-java) [![Coverage Status](https://coveralls.io/repos/libgraviton/graviton-worker-base-java/badge.svg?branch=develop&service=github)](https://coveralls.io/github/libgraviton/graviton-worker-base-java?branch=develop) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.libgraviton/worker-base/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.libgraviton/worker-base) [![javadoc.io](https://javadocio-badges.herokuapp.com/com.github.libgraviton/worker-base/badge.svg)](https://javadocio-badges.herokuapp.com/com.github.libgraviton/worker-base) 

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

## API Doc

Please see the [apidoc.io apidoc](http://www.javadoc.io/doc/com.github.libgraviton/worker-base) ;-)

## Using the library

You can use this library in your project by including this in your `pom.xml`:

```xml
<dependencies>
	<dependency>
		<groupId>com.github.libgraviton</groupId>
		<artifactId>worker-base</artifactId>
		<version>0.4.0</version>
	</dependency>
</dependencies>	
```

make sure that `version` points to the newest release on Maven central (see badge above).

## Deploying a release

This package is built *without* shade plugin, but ships with Atlassian `jgit-flow`. The goals are:

```
mvn jgitflow:release-start
mvn jgitflow:release-finish
```

After the release-finish, it will try to deploy to `ossrh` automatically. Please make sure you only do this if everything is configured as expected.

```
mvn -DperformRelease=true deploy
```