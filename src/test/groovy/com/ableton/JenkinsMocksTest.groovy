package com.ableton

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
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

  @Test
  void shWithScriptFailure() throws Exception {
    boolean exceptionThrown = false
    try {
      JenkinsMocks.addShMock('evil', '/foo/bar', 666)
      JenkinsMocks.sh('evil')
    } catch (error) {
      exceptionThrown = true
      assertNotNull(error)
    }
    assertTrue(exceptionThrown)
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
    boolean exceptionThrown = false
    try {
      JenkinsMocks.sh('invalid')
    } catch (IllegalArgumentException error) {
      exceptionThrown = true
      assertNotNull(error)
    }
    assertTrue(exceptionThrown)
  }

  @Test
  void shWithBothStatusAndStdout() throws Exception {
    boolean exceptionThrown = false
    try {
      JenkinsMocks.sh(returnStatus: true, returnStdout: true, script: 'invalid')
    } catch (IllegalArgumentException error) {
      exceptionThrown = true
      assertNotNull(error)
    }
    assertTrue(exceptionThrown)
  }
}
