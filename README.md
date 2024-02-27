# Mock-All

A utility library built on top of [Mockito](https://github.com/mockito/mockito) to simplify context initialisation for test classes.
Ideally this library should not be required, but in scenarios where you want to setup isolated test environments quickly this is an option.

By default any marked tests will have all Spring wired objects mocked unless specified otherwise.

## Including Dependency

This can be included by making sure that you have [JitPack](https://jitpack.io) setup within your project. You can refer to the hosted versions of this library at [Mock-All](https://jitpack.io/#Kilemonn/Mock-All).
Once added you can include this repository based on its release version (review to versions list) or with other syntax like <branch-name>-SNAPSHOT OR via <commit-hash> as the version segment of the depdency import.

Here is an example in Gradle to include the dependency for test:
```
testImplementation("com.github.Kilemonn:mock-all:0.1.2")
```

## Usage

There are two components to this library that when used together can allow you to control which beans are excempt from the auto mocking and which beans will be created as `spy`s.

### Mock all

Firstly you need to mark the test class with the following annotation:
```
@TestExecutionListeners(MockAllExecutionListener::class)
```

With only this setup all wired beans will be automatically mocked.

### Skip Mock

In more scenarios you would only want to skip mocking on objects within the test class directly. Generally other context beans may not be in scope for direct testing. 
If you want to skip wiring of any object you can add this annotation to that member. This annotation is used later to drive spy beans.
```
@NotMocked
```

### Initialise as Spy

Similarly to how objects are marked to skip, you can provide a list of classes for Object types that should be initialised as spy objects during the initialisation.
```
@NotMocked([PermissionChecker.class, RoleChecker.class])
```

Please refer to the unit tests for examples of how this mocking is put into action.

### Accessing Mocked/Spyed Instances

Mocked/Spied instances can be requested via the static method:
```
MockAllExecutionListener.getInstance(clazz: Class<*>): Any?
```

If there is inheritance involved, make sure you provide the exact class name to look up and return from the mock map.
