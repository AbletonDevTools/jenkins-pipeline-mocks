package com.ableton

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Test


/**
 * Tests for the JenkinsMocks class.
 */
class JenkinsMocksTest extends BasePipelineTest {
  @Test
  void echo() throws Exception {
    // Just a sanity check test to make sure nothing throws
    JenkinsMocks.echo('test')
  }

  @Test
  void error() throws Exception {
    try {
      JenkinsMocks.error('test')
      fail('Expected exception, but none was thrown')
    } catch (error) {
      assertEquals('test', error.message)
    }
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
    assertNotNull(result)
    File f = new File(result)
    assertTrue(f.exists())
  }

  @Test
  void pwdTmp() throws Exception {
    String result = JenkinsMocks.pwd(tmp: true)
    assertNotNull(result)
    File f = new File(result)
    assertTrue(f.exists())
  }

  @Test
  void retry() throws Exception {
    def bodyExecuted = false
    JenkinsMocks.retry(2) {
      bodyExecuted = true
    }
    assertTrue(bodyExecuted)
  }

  @Test
  @SuppressWarnings('ThrowException')
  void retryFailOnce() throws Exception {
    def count = 2
    def exceptionThrown = false
    def bodyExecuted = false
    JenkinsMocks.retry(2) {
      if (count-- == 2) {
        exceptionThrown = true
        throw new Exception()
      }
      bodyExecuted = true
    }
    assertTrue(exceptionThrown)
    assertTrue(bodyExecuted)
  }

  @Test
  @SuppressWarnings('ThrowException')
  void retryFail() throws Exception {
    def failed = false
    def count = 2
    try {
      JenkinsMocks.retry(2) {
        count--
        throw new Exception('test')
      }
    } catch (error) {
      assertEquals('test', error.message)
      failed = true
    }
    assertEquals(0, count)
    assertTrue(failed)
  }

  @Test
  void sh() throws Exception {
    JenkinsMocks.addShMock('pwd', '/foo/bar', 0)
    assertTrue(JenkinsMocks.sh('pwd'))
  }

  @Test
  void shWithScriptFailure() throws Exception {
    JenkinsMocks.addShMock('evil', '/foo/bar', 666)
    assertFalse(JenkinsMocks.sh('evil'))
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
  void shWithoutMockScript() throws Exception {
    try {
      JenkinsMocks.sh('invalid')
      fail('Expected exception, but none was thrown')
    } catch (IllegalArgumentException error) {
      assertNotNull(error)
    }
  }

  @Test
  void shWithBothStatusAndStdout() throws Exception {
    try {
      JenkinsMocks.sh(returnStatus: true, returnStdout: true, script: 'invalid')
      fail('Expected exception, but none was thrown')
    } catch (IllegalArgumentException error) {
      assertNotNull(error)
    }
  }
}
