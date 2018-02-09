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

  @Test
  void clearStaticData() throws Exception {
    JenkinsMocks.addShMock('test', '', 0)
    assertEquals(1, JenkinsMocks.mockScriptOutputs.size())
    JenkinsMocks.setCatchErrorParent(this)
    assertNotNull(JenkinsMocks.catchErrorParent)
    assertNotNull(JenkinsMocks.catchErrorUpdateBuildStatus)

    JenkinsMocks.clearStaticData()

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

  @Test
  void echo() throws Exception {
    // Just a sanity check test to make sure nothing throws
    JenkinsMocks.echo('test')
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
  void retry() throws Exception {
    int bodyExecutedCount = 0
    JenkinsMocks.retry(2) {
      bodyExecutedCount++
    }
    assertEquals(1, bodyExecutedCount)
  }

  // We don't use the @Test(expected) annotation here because we want to verify the number
  // of times the closure body executed.
  @Test
  @SuppressWarnings('ThrowException')
  void retryFailOnce() throws Exception {
    int count = 2
    boolean exceptionThrown = false
    int bodyExecutedCount = 0
    JenkinsMocks.retry(2) {
      bodyExecutedCount++
      if (count-- == 2) {
        exceptionThrown = true
        throw new Exception()
      }
    }
    assertTrue(exceptionThrown)
    assertEquals(2, bodyExecutedCount)
  }

  @Test
  @SuppressWarnings('ThrowException')
  void retryFail() throws Exception {
    int bodyExecutedCount = 0
    boolean failed = false
    int count = 2
    try {
      JenkinsMocks.retry(2) {
        bodyExecutedCount++
        count--
        throw new Exception('test')
      }
    } catch (error) {
      assertEquals('test', error.message)
      failed = true
    }
    assertEquals(2, bodyExecutedCount)
    assertEquals(0, count)
    assertTrue(failed)
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

  @Test(expected = IllegalArgumentException)
  void shWithoutMockScript() throws Exception {
    JenkinsMocks.sh('invalid')
  }

  @Test(expected = IllegalArgumentException)
  void shWithBothStatusAndStdout() throws Exception {
    JenkinsMocks.sh(returnStatus: true, returnStdout: true, script: 'invalid')
  }
}
