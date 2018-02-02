import com.ableton.DockerMock as DockerMock


void execute() {
  node {
    String testImageName = 'test'

    stage('Test docker.build()') {
      DockerMock.Image image = docker.build(testImageName)
      assert image.id == testImageName
    }

    stage('Test docker.image()') {
      DockerMock.Image image = docker.image(testImageName)
      assert image.id == testImageName
    }

    stage('Test docker.withRegistry') {
      docker.withRegistry('test.registry', 'fake-credentials') {
        DockerMock.Image image = docker.image(testImageName)
        // TODO: Should the registry URL be prepended to the image name here?
        assert image.id == testImageName
      }
    }

    stage('Test docker.withServer()') {
      docker.withServer('test.server', 'fake-credentials') {
        DockerMock.Image image = docker.image(testImageName)
        assert image.id == testImageName
      }
    }

    stage('Test docker.withTool()') {
      docker.withTool('test-docker') {
        DockerMock.Image image = docker.image(testImageName)
        assert image.id == testImageName
      }
    }

    stage('Test docker.image.imageName()') {
      DockerMock.Image image = docker.image(testImageName)
      assert image.imageName() == testImageName
    }

    stage('Test docker.image.inside()') {
      DockerMock.Image image = docker.image(testImageName)
      image.inside {
        assert image.id == testImageName
      }
    }

    stage('Test docker.image.pull()') {
      docker.image(testImageName).pull()
    }

    stage('Test docker.image.push()') {
      DockerMock.Image image = docker.image(testImageName)
      image.push('test-tag')
      assert image.tagname == 'test-tag'
    }

    stage('Test docker.image.run()') {
      DockerMock.Container container = docker.image(testImageName).run()
      container.stop()
    }

    stage('Test docker.image.tag()') {
      DockerMock.Image image = docker.image(testImageName)
      image.tag('test')
      assert image.tagname == 'test'
    }

    stage('Test docker.image.withRun()') {
      DockerMock.Image image = docker.image(testImageName)
      image.withRun {
        assert image.id == testImageName
      }
    }
  }
}

return this
