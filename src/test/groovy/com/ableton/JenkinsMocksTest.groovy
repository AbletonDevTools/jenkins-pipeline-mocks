package com.ableton

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * Tests for the JenkinsMocks class.
 */
@SuppressWarnings('MethodCount')
class JenkinsMocksTest extends BasePipelineTest {
  // The setUp() method in the super class is not annotated with @Before since
  // JenkinsPipelineUnit does not directly depend on the JUnit framework. As such, we need
  // this otherwise empty override in order to configure variable bindings.
  @SuppressWarnings('UnnecessaryOverridingMethod')
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @After
  void tearDown() throws Exception {
    JenkinsMocks.clearStaticData()
  }

  @SuppressWarnings('UnnecessarySetter')
  @Test
  void clearStaticData() throws Exception {
    JenkinsMocks.addReadFileMock('test', 'contents')
    assertEquals(1, JenkinsMocks.mockReadFileOutputs.size())
    JenkinsMocks.addShMock('test', '', 0)
    assertEquals(1, JenkinsMocks.mockScriptOutputs.size())
    JenkinsMocks.setCatchErrorParent(this)
    assertNotNull(JenkinsMocks.catchErrorParent)
    assertNotNull(JenkinsMocks.catchErrorUpdateBuildStatus)

    JenkinsMocks.clearStaticData()

    assertEquals(0, JenkinsMocks.mockReadFileOutputs.size())
    assertEquals(0, JenkinsMocks.mockScriptOutputs.size())
    assertNull(JenkinsMocks.catchErrorParent)
    assertNull(JenkinsMocks.catchErrorUpdateBuildStatus)
  }

  @SuppressWarnings(['ThrowException', 'UnnecessarySetter'])
  @Test
  void catchError() throws Exception {
    JenkinsMocks.setCatchErrorParent(this)
    JenkinsMocks.catchError {
      throw new Exception('test')
    }
    assertEquals('FAILURE', binding.getVariable('currentBuild').result)
  }

  @SuppressWarnings('ThrowException')
  @Test(expected = IllegalArgumentException)
  void catchErrorWithoutParent() throws Exception {
    JenkinsMocks.catchError {
      throw new Exception('test')
    }
  }

  @SuppressWarnings(['ThrowException', 'UnnecessarySetter'])
  @Test(expected = NoSuchMethodException)
  void catchErrorWithWrongParentClass() throws Exception {
    JenkinsMocks.setCatchErrorParent(new Object())
  }

  // We don't use the @Test(expected) annotation here because we want to verify the
  // contents of the exception message.
  @Test
  void error() throws Exception {
    boolean exceptionThrown = false
    try {
      JenkinsMocks.error('test')
    } catch (error) {
      exceptionThrown = true
      assertEquals('test', error.message)
    }
    assertTrue(exceptionThrown)
  }

  @Test
  void isUnix() throws Exception {
    // It would be pretty redundant to basically re-implement this method in its own test
    // case, so instead we just call the function and see that it didn't go haywire.
    JenkinsMocks.isUnix()
  }

  @Test
  void pwd() throws Exception {
    String result = JenkinsMocks.pwd()
    assertEquals(System.properties['user.dir'], result)
    File f = new File(result)
    assertTrue(f.exists())
  }

  @Test
  void pwdTmp() throws Exception {
    String result = JenkinsMocks.pwd(tmp: true)
    assertEquals(System.properties['java.io.tmpdir'], result)
    File f = new File(result)
    assertTrue(f.exists())
  }

  @Test
  void readFile() throws Exception {
    JenkinsMocks.addReadFileMock('test', 'contents')
    assertEquals('contents', JenkinsMocks.readFile('test'))
  }

  @Test
  void readFileWithMap() throws Exception {
    JenkinsMocks.addReadFileMock('test', 'contents')
    assertEquals('contents', JenkinsMocks.readFile(file: 'test'))
  }

  @Test(expected = IllegalArgumentException)
  void readFileWithNoMock() throws Exception {
    JenkinsMocks.readFile('test')
  }

  @Test
  void sh() throws Exception {
    JenkinsMocks.addShMock('pwd', '/foo/bar', 0)
    assertNull(JenkinsMocks.sh('pwd'))
  }

  @Test(expected = Exception)
  void shWithScriptFailure() throws Exception {
    JenkinsMocks.addShMock('evil', '/foo/bar', 666)
    JenkinsMocks.sh('evil')
  }

  @Test
  void shWithStdout() throws Exception {
    JenkinsMocks.addShMock('pwd', '/foo/bar', 0)
    assertEquals('/foo/bar', JenkinsMocks.sh(returnStdout: true, script: 'pwd'))
  }

  @Test
  void shWithReturnCode() throws Exception {
    JenkinsMocks.addShMock('pwd', '/foo/bar', 0)
    assertEquals(0, JenkinsMocks.sh(returnStatus: true, script: 'pwd'))
  }

  @Test
  void shWithNonZeroReturnCode() throws Exception {
    JenkinsMocks.addShMock('evil', '/foo/bar', 666)
    assertEquals(666, JenkinsMocks.sh(returnStatus: true, script: 'evil'))
  }

  @Test
  void shWithCallback() throws Exception {
    JenkinsMocks.addShMock('pwd') { script ->
      return [stdout: '/foo/bar', exitValue: 0]
    }
    assertNull(JenkinsMocks.sh('pwd'))
  }

  @Test(expected = Exception)
  void shWithCallbackScriptFailure() throws Exception {
    JenkinsMocks.addShMock('evil') { script ->
      return [stdout: '/foo/bar', exitValue: 666]
    }
    JenkinsMocks.sh('evil')
  }

  @Test
  void shWithCallbackStdout() throws Exception {
    JenkinsMocks.addShMock('pwd') { script ->
      return [stdout: '/foo/bar', exitValue: 0]
    }
    assertEquals('/foo/bar', JenkinsMocks.sh(returnStdout: true, script: 'pwd'))
  }

  @Test
  void shWithCallbackReturnCode() throws Exception {
    JenkinsMocks.addShMock('pwd') { script ->
      return [stdout: '/foo/bar', exitValue: 0]
    }
    assertEquals(0, JenkinsMocks.sh(returnStatus: true, script: 'pwd'))
  }

  @Test
  void shWithCallbackNonZeroReturnCode() throws Exception {
    JenkinsMocks.addShMock('pwd') { script ->
      return [stdout: '/foo/bar', exitValue: 666]
    }
    assertEquals(666, JenkinsMocks.sh(returnStatus: true, script: 'pwd'))
  }

  @Test(expected = IllegalArgumentException)
  void shWithCallbackOutputNotMap() throws Exception {
    JenkinsMocks.addShMock('pwd') { script ->
      return 'invalid'
    }
    JenkinsMocks.sh(returnStatus: true, script: 'pwd')
  }

  @Test(expected = IllegalArgumentException)
  void shWithCallbackNoStdoutKey() throws Exception {
    JenkinsMocks.addShMock('pwd') { script ->
      return [exitValue: 666]
    }
    JenkinsMocks.sh(returnStatus: true, script: 'pwd')
  }

  @Test(expected = IllegalArgumentException)
  void shWithCallbackNoExitValueKey() throws Exception {
    JenkinsMocks.addShMock('pwd') { script ->
      return [stdout: '/foo/bar']
    }
    JenkinsMocks.sh(returnStatus: true, script: 'pwd')
  }

  @Test(expected = IllegalArgumentException)
  void shWithoutMockScript() throws Exception {
    JenkinsMocks.sh('invalid')
  }

  @Test(expected = IllegalArgumentException)
  void shWithBothStatusAndStdout() throws Exception {
    JenkinsMocks.sh(returnStatus: true, returnStdout: true, script: 'invalid')
  }
}
