# Mock-All

[![CI Build](https://github.com/Kilemonn/Mock-All/actions/workflows/gradle.yml/badge.svg)](https://github.com/Kilemonn/Mock-All/actions/workflows/gradle.yml) [![Coverage](.github/badges/jacoco.svg)](https://github.com/Kilemonn/Mock-All/actions/workflows/gradle.yml)

A test mock utility library built on top of [Mockito](https://github.com/mockito/mockito) to simplify context configuration for test classes.
Ideally this library should not be required, but in scenarios where you want to setup isolated test configurations quickly this is an option especially when working with existing code with limited tests.

## Including Dependency

This can be included by making sure that you have [JitPack](https://jitpack.io) setup within your project. You can refer to the hosted versions of this library at [Mock-All](https://jitpack.io/#Kilemonn/Mock-All).

Here is an example in Gradle to include the dependency for test:
```
testImplementation("com.github.Kilemonn:mock-all:0.1.4")
```

## Usage

There are two components to this library that when used together that allows you to control which beans are excempt from the auto mocking (allowing a default initialised bean to be used) and which beans will be created as a `spy`.

Make sure you add the following configuration to your maven/gradle test configuration so that reflection can be used.
Here is an example for gradle:
```
jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED", "--add-opens", "java.base/java.lang=ALL-UNNAMED")
```

### Mock all

Firstly you need to mark the test class with the following annotation:
```java
@TestExecutionListeners(MockAllExecutionListener::class)
```

**With only this annotation present all wired beans in the test class and linked in test contexts will be automatically mocked.**

### Skip Mocking (Create actual objects)

There are likely scenarios where you would want to make sure actual objects are used instead of mocks. Most likely the class(es) you are actually testing.
If you want to skip wiring of any object you can add this annotation to that member. This annotation is used later to drive spy beans.

```java
@NotMocked
```

Currently these objects annotated with `@NotMocked` will be created via its default zero argument constructor.

### Initialise as Spy

Similarly to how objects are marked to be created as actual objects, you can provide a list of classes that should be initialised as spy objects instead of mocks during the initialisation.
```java
@NotMocked([PermissionChecker.class, RoleChecker.class])
```

Please refer to the unit tests for examples of how this mocking is put into action.

**NOTE: The following will result in "MyObject" being created as a spy because it is provided in the spy object list.**
```java
@Autowired
@NotMocked([MyObject.class])
private MyObject obj;
```

### Accessing Mocked/Spyed Instances

Mocked/Spied instances can be requested via the static method:
```java
MockAllExecutionListener.getInstance(clazz: Class<*>): Any?
```

If there is inheritance involved, there are other utility methods in `MockAllExecutionListener` to work with hierarchy object look up.
