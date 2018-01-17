package com.ableton

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test


/**
 * Test the DockerMock class
 *
 * It would be a bit redundant to write regular unit tests for DockerMock, because most of
 * the methods are empty or void. So instead this test class loads a Jenkins pipeline file
 * which exercises all of the methods of the docker singleton.
 */
class DockerMockTest extends BasePipelineTest {
  def script

  @Before
  @Override
  void setUp() {
    super.setUp()
    this.script = loadScript('src/test/resources/DockerPipeline.groovy')
    this.script.docker = new DockerMock()
  }

  @Test
  void executePipeline() throws Exception {
    script.execute()
  }
}
