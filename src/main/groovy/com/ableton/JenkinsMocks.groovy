package com.ableton

import java.lang.reflect.Method


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

  static Closure archive = { /* noop */ }

  static Closure deleteDir = { /* noop */ }

  static Closure dir = { String path, Closure body ->
    body()
  }

  static Closure echo = { String message ->
    println message
  }

  @SuppressWarnings('ThrowException')
  static Closure error = { String message ->
    throw new Exception(message)
  }

  static Closure isUnix = {
    return !System.properties['os.name'].toLowerCase().contains('windows')
  }

  static Closure mail = { /* noop */ }

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

  static Closure retry = { count, body ->
    Exception lastError = null
    while (count-- > 0) {
      try {
        body()
        lastError = null
        break
      } catch (error) {
        lastError = error
      }
    }

    if (lastError) {
      throw lastError
    }
  }

  /**
   * Simple container for holding mock script output.
   * @see JenkinsMocks#addShMock
   */
  class MockScriptOutput {
    String stdout
    int exitValue

    MockScriptOutput(String stdout, int exitValue) {
      this.stdout = stdout
      this.exitValue = exitValue
    }
  }

  /** Holds configured mock output values for the `sh` command. */
  static Map<String, MockScriptOutput> mockScriptOutputs = [:]

  /** Holds configured mock callback values for the `sh` command. */
  static Map<String, Closure> rerouteScriptWithCallbacks = [:]

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
   * @param scriptIncludeKeyword Full script command or part of it to mock. Note script match occurs with cmd.contains() so can forward e.g. specific tool commands to single mock callback despite of different parameters.
   * @param callback Closure to be called when script keyword matches
   * @return
   */
  static void addShMockCallback(String scriptIncludeKeyword, Closure callback) {
    rerouteScriptWithCallbacks[scriptIncludeKeyword] = callback
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

    //Find 1st matching callback if exist
    def mockCallbacks = rerouteScriptWithCallbacks.findAll { keyword, mockCallback ->
        script.contains(keyword)
    }.collect { keyword, mockCallback ->
        mockCallback
    }
    if(mockCallbacks.size() > 1){
        throw new IllegalArgumentException(
          "More than 1 addShMockCallback() match with '${script}' - need to refine keyword to match single mock")
    }
    if(!mockCallbacks?.isEmpty()) {
        return mockCallbacks?.first().call(script, returnStdout, returnStatus)
    }

    MockScriptOutput output = mockScriptOutputs[script]
    if (!output) {
      throw new IllegalArgumentException('No mock output configured for script call ' +
        "'${script}', did you forget to call JenkinsMocks.addShMock()?")
    }
    if (!returnStdout) {
      println output.stdout
    }

    if (returnStdout) {
      return output.stdout
    }
    if (returnStatus) {
      return output.exitValue
    }
    if (output.exitValue != 0) {
      throw new Exception('Script returned error code: ' + output.exitValue)
    }
  }

  static def waitUntil(int maxTimes, Closure body) {
      int count = 0
      while(body() == false){
          count++
          if(count > maxTimes){
              throw new Exception("waitUntil: failed due to could not resolve in ${maxTimes} loops")
          }
      }
  }
  static def waitUntil = { body ->
      waitUntil(100, body)
  }

  static Closure sleep = { /* noop */ }

  static Closure stash = { /* noop */ }

  static Closure unstash = { /* noop */ }

  static Closure writeFile = { /* noop */ }
}
