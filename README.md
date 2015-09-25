# graviton-worker-base-java

[![Build Status](https://travis-ci.org/libgraviton/graviton-worker-base-java.svg?branch=develop)](https://travis-ci.org/libgraviton/graviton-worker-base-java) [![Coverage Status](https://coveralls.io/repos/libgraviton/graviton-worker-base-java/badge.svg?branch=develop&service=github)](https://coveralls.io/github/libgraviton/graviton-worker-base-java?branch=develop) [![Github tag](https://img.shields.io/github/tag/libgraviton/graviton-worker-base-java.svg)](https://github.com/libgraviton/graviton-worker-base-java/tags) [![Maven central](https://img.shields.io/maven-central/v/com.github.libgraviton/worker-base.svg)]() 

## Using the library

You can use this library in your project by including this in your `pom.xml`:

```xml
<dependencies>
	<dependency>
		<groupId>com.github.libgraviton</groupId>
		<artifactId>worker-base</artifactId>
		<version>0.1.0</version>
	</dependency>
</dependencies>	
```

make sure that `version` points to the newest release on Maven central.

## Deploying a release

This package is built *without* shade plugin, but ships with Atlassian `jgit-flow`. The goals are:

```
mvn jgitflow:release-start
mvn jgitflow:release-finish
```

it is configured to call `mvn install` instead of deploy. After you ran the release finish command and switched to master, deploy your release by issuing

```
mvn -DperformRelease=true deploy
```