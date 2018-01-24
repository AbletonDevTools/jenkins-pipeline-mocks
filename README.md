# jenkins-pipeline-mocks

`jenkins-pipeline-mocks` is a Groovy library which contains mock objects for use with the
[JenkinsPipelineUnit][jenkins-pipeline-unit] library. When using JenkinsPipelineUnit, any
pipeline steps normally provided by Jenkins must be mocked. This library provides some
convenient and common mock objects to be used in such cases.

## Using the Library

### Gradle

The easiest way to use this library with your project is to add the following lines to
your `build.gradle` file:

```groovy
repositories {
  maven {
    url 'https://jitpack.io'
  }
}

dependencies {
  compile 'com.github.AbletonDevTools:jenkins-pipeline-mocks:X.Y.Z'
}
```

Where `X.Y.Z` corresponds to the version of the library you want to use.

### Example usage in unit tests

To use the mocks in your test cases, you'll need to configure them in your test class.
This can be done like so:

```groovy
import com.ableton.JenkinsMocks

class TestClass extends BasePipelineTest {
  @Override
  @Before
  void setUp() {
    super.setUp()
    helper.registerAllowedMethod('isUnix', [], JenkinsMocks.isUnix)
    helper.registerAllowedMethod('pwd', [Map], JenkinsMocks.pwd)
    helper.registerAllowedMethod('sh', [String], JenkinsMocks.sh)
  }
}
```

### Mocking the `sh` command

While most of the mocks implement the same functionality as their Jenkins counterparts
(for instance, `isUnix` will return `true` on Unix systems and `false` otherwise), the
`sh` mock does not execute commands. Instead, you must configure the `sh` mock to provide
it with the mock output that it should return for a certain command. This is done by
calling the `addShMock()` function, which can be invoked either in your `setUp()` method
or for an individual test case.

```groovy
import com.ableton.JenkinsMocks

class TestClass extends BasePipelineTest {
  @Override
  @Before
  void setUp() {
    super.setUp()
    helper.registerAllowedMethod('sh', [String], JenkinsMocks.sh)
  }

  @Test
  void ls() throws Exception {
    // Will return 'foo' when pipeline calls: sh 'ls /tmp'
    JenkinsMocks.addShMock('ls /tmp', 'foo', 0)
    def script = loadScript('lstest.groovy')
    script.execute()
  }

  @Test
  void lsInvalid() throws Exception {
    // Will fail with return code 1 when pipeline calls: sh 'ls /invalid'
    JenkinsMocks.addShMock('ls /invalid', '', 1)
    def script = loadScript('lstest.groovy')
    script.execute()
  }
}
```

## Building and Testing

The `jenkins-pipeline-mocks` library can be developed locally using the provided Gradle
wrapper. Likewise, the Gradle project can be imported by an IDE like IntelliJ IDEA. For
this, you'll need the Groovy plugin enabled in IDEA and to install Groovy SDK.


[jenkins-pipeline-unit]: https://github.com/jenkinsci/JenkinsPipelineUnit
