# Order service

This project is part of my demonstration project. Like any other sub-project, it is meant to show my abilities to design and maintain a system, write qualitative code (including tests, of course), learn and use some of the technologies which I use on a daily basis at work and be one more reason to explore the latest changes of used frameworks and libraries.

The role of this sub-project application in the whole project is the backend service for the order context in project's domain model.

## Features
   
   * Java 11
   * lombok
   * Spring Framework 5
   * Spring Boot 2
   * MongoDB
   * Problem Details [RFC 7807](https://tools.ietf.org/html/rfc7807)
   * HATEOAS (JSON HAL)

### Concurrent Updates Management

PATCH

### I18n
English only

### Prerequisites

To build the source you will need to install JDK 11.

**NOTE**: You can also install Maven (>=3.3.3) yourself and run the `mvn` command in place of `./mvnw`.

### Installing

```
$ ./mvnw install
```

**NOTE**: Be aware that you might need to increase the amount of memory available to Maven by setting a `MAVEN_OPTS` environment variable with a value like `-Xmx512m -XX:MaxPermSize=128m`.

## Running

So far (it's requires the running auth-service):
```
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
