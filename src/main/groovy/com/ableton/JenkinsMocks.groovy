package com.ableton

import java.lang.reflect.Method
import org.codehaus.groovy.runtime.typehandling.GroovyCastException


/**
 * Provides functional mocks for some Jenkins functions, which is useful in combination
 * with the JenkinsPipelineUnit library.
 */
class JenkinsMocks {
  /**
   * Clears static data used by some closures in this class. If you use any of the
   * following functions, you should call this function in your tearDown method:
   * <ul>
   *   <li>{@link JenkinsMocks#addShMock}</li>
   *   <li>{@link JenkinsMocks#setCatchErrorParent}</li>
   * </ul>
   */
  static void clearStaticData() {
    mockReadFileOutputs.clear()
    mockScriptOutputs.clear()
    catchErrorParent = null
    catchErrorUpdateBuildStatus = null
  }

  static Object catchErrorParent = null
  static Method catchErrorUpdateBuildStatus = null

  static void setCatchErrorParent(Object parent) {
    catchErrorUpdateBuildStatus = parent.getClass().getMethod('updateBuildStatus', String)
    catchErrorParent = parent
  }

  static Closure catchError = { Closure body ->
    try {
      body()
    } catch (ignored) {
      if (catchErrorParent && catchErrorUpdateBuildStatus) {
        catchErrorUpdateBuildStatus.invoke(catchErrorParent, 'FAILURE')
      } else {
        throw new IllegalArgumentException('No parent object has been registered for ' +
          'catchError, did you forget to call setCatchErrorParent()?')
      }
    }
  }

  @SuppressWarnings('ThrowException')
  static Closure error = { String message ->
    throw new Exception(message)
  }

  static Closure isUnix = {
    return !System.properties['os.name'].toLowerCase().contains('windows')
  }

  static Closure pwd = { args = [:] ->
    return System.properties[args?.tmp ? 'java.io.tmpdir' : 'user.dir']
  }

  static Map<String, String> mockReadFileOutputs = [:]

  static void addReadFileMock(String file, String contents) {
    mockReadFileOutputs[file] = contents
  }

  static Closure readFile = { args ->
    String file = null
    if (args instanceof String || args instanceof GString) {
      file = args
    } else if (args instanceof Map) {
      file = args['file']
    }
    assert file

    if (!mockReadFileOutputs.containsKey(file)) {
      throw new IllegalArgumentException("No mock output configured for '${file}', " +
        'did you forget to call JenkinsMocks.addReadFileMock()?')
    }
    return mockReadFileOutputs[file]
  }

  /**
   * Simple container for holding mock script output.
   * @see JenkinsMocks#addShMock
   */
  class MockScriptOutput {
    String stdout = null
    int exitValue = -1
    Closure callback = null

    MockScriptOutput(String stdout, int exitValue) {
      this.stdout = stdout
      this.exitValue = exitValue
    }

    MockScriptOutput(Closure callback) {
      this.callback = callback
    }
  }

  /** Holds configured mock output values for the `sh` command. */
  static Map<String, MockScriptOutput> mockScriptOutputs = [:]

  /**
   * Configure mock output for the `sh` command. This function should be called before
   * attempting to call `JenkinsMocks.sh()`.
   * @see JenkinsMocks#sh
   * @param script Script command to mock.
   * @param stdout Standard output text to return for the given command.
   * @param exitValue Exit value for the command.
   * @return
   */
  static void addShMock(String script, String stdout, int exitValue) {
    mockScriptOutputs[script] = new MockScriptOutput(null, stdout, exitValue)
  }

  /**
   * Configure mock callback for the `sh` command. This function should be called before
   * attempting to call `JenkinsMocks.sh()`.
   * @see JenkinsMocks#sh
   * @param script Script command to mock.
   * @param callback Closure to be called when the mock is executed. This closure will be
   *                 passed the script call which is being executed, and
   *                 <strong>must</strong> return a {@code Map} with the following
   *                 key/value pairs:
   *                 <ul>
   *                   <li>{@code stdout}: {@code String} with the mocked output.</li>
   *                   <li>{@code exitValue}: {@code int} with the mocked exit value.</li>
   *                 </ul>
   */
  static void addShMock(String script, Closure callback) {
    mockScriptOutputs[script] = new MockScriptOutput(null, callback)
  }

  @SuppressWarnings('ThrowException')
  static Closure sh = { args ->
    String script = null
    boolean returnStdout = false
    boolean returnStatus = false

    // The `sh` function can be called with either a string, or a map of key/value pairs.
    if (args instanceof String || args instanceof GString) {
      script = args
    } else if (args instanceof Map) {
      script = args['script']
      returnStatus = args['returnStatus'] ?: false
      returnStdout = args['returnStdout'] ?: false
      if (returnStatus && returnStdout) {
        throw new IllegalArgumentException(
          'returnStatus and returnStdout are mutually exclusive options')
      }
    }
    assert script

    MockScriptOutput output = mockScriptOutputs[script]
    if (!output) {
      throw new IllegalArgumentException('No mock output configured for script call ' +
        "'${script}', did you forget to call JenkinsMocks.addShMock()?")
    }

    String stdout
    int exitValue

    // If the callback closure is not null, execute it and grab the output.
    if (output.callback) {
      Map callbackOutput
      try {
        callbackOutput = output.callback(script)
      } catch (GroovyCastException) {
        throw new IllegalArgumentException("Mocked sh callback for ${script}" +
          ' was not a map')
      }
      if (!callbackOutput.containsKey('stdout')
        || !(callbackOutput['stdout'] instanceof String)) {
        throw new IllegalArgumentException("Mocked sh callback for ${script} did not" +
          ' contain a valid value for the stdout key')
      }
      if (!callbackOutput.containsKey('exitValue')
        || !(callbackOutput['exitValue'] instanceof Integer)) {
        throw new IllegalArgumentException("Mocked sh callback for ${script} did not" +
          ' contain a valid value for the exitValue key')
      }
      stdout = callbackOutput['stdout']
      exitValue = callbackOutput['exitValue']
    } else {
      stdout = output.stdout
      exitValue = output.exitValue
    }

    if (!returnStdout) {
      println stdout
    }

    if (returnStdout) {
      return stdout
    }
    if (returnStatus) {
      return exitValue
    }
    if (exitValue != 0) {
      throw new Exception('Script returned error code: ' + exitValue)
    }
  }
}
