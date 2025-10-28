[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=20403271)
# Mutual Inclusion -- CSCI 602 -- Fall 2025 Group Project

This is the repository for the CSCI 602 Group Project that consists of the
following students:

+ Christian Soto
+ Dan Johnson
+ Erin McCall
+ Tuck Williamson

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)

## Environment Setup (Students)

1.) Install Java JDK 17+. JDK located [here](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html). If you have a Mac you can use `brew`.

```bash
brew install --cask corretto@17
```

If you're on Windows, download the `.msi` installer from [here](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html) and run.

2.) Create your personal repository from Github. You should receive a unique link from your professor from Github Classroom that will generate a private repository within your Github account.
It should look something like this.

```bash
https://classroom.github.com/a/{classroomId}
```

3.) This will create your personal repository within the [Citadel CS Github Organization](https://github.com/CitadelCS).

4.) Clone down the repository from Github

```bash
git clone git@git.github.com:CitadelCS/csci-602-fall-2021-{yourUsername}.git
```

5.) Build the project

> Disclaimer: If running on a Windows machine replace `./mvnw` with `.\mvnw`

```bash
./mvnw clean install
```

You should see a success if everything is set up correctly.

6.) Run the API

```bash
./mvnw spring-boot:run
```

Access the API by visiting [http://localhost:5001/swagger-ui/index.html](http://localhost:5001/swagger-ui/index.html). From there you can hit the endpoints directly.

7.) Success!

## Setting Up DataSource

Setting up the datasource within IntelliJ should be straightforward with username and password. If you're using a
Heroku datasource then you will need to set the following settings on the Advanced tab

![Datasource](images/datasource_settings.png)

## Resources

### Spring Boot

For further references with Spring Boot:

- [Spring Initializr](https://start.spring.io/)
- [Getting Started](https://spring.io/guides/gs/spring-boot/)

### Maven

For further references with Maven's dependency management framework:

- [Spring and Maven](https://spring.io/guides/gs/spring-boot/)
- [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
- [Apache Maven Getting Started](https://maven.apache.org/guides/getting-started/)

