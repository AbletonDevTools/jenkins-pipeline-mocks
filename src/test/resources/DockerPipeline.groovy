def execute() {
  node {
    def testImageName = 'test'

    stage('Test docker.build()') {
      def image = docker.build(testImageName)
      assert image.id == testImageName
    }

    stage('Test docker.image()') {
      def image = docker.image(testImageName)
      assert image.id == testImageName
    }

    stage('Test docker.withRegistry') {
      docker.withRegistry('test.registry', 'fake-credentials') {
        def image = docker.image(testImageName)
        // TODO: Should the registry URL be prepended to the image name here?
        assert image.id == testImageName
      }
    }

    stage('Test docker.withServer()') {
      docker.withServer('test.server', 'fake-credentials') {
        def image = docker.image(testImageName)
        assert image.id == testImageName
      }
    }

    stage('Test docker.withTool()') {
      docker.withTool('test-docker') {
        def image = docker.image(testImageName)
        assert image.id == testImageName
      }
    }

    stage('Test docker.image.imageName()') {
      def image = docker.image(testImageName)
      assert image.imageName() == testImageName
    }

    stage('Test docker.image.inside()') {
      def image = docker.image(testImageName)
      image.inside {
        assert image.id == testImageName
      }
    }

    stage('Test docker.image.pull()') {
      docker.image(testImageName).pull()
    }

    stage('Test docker.image.push()') {
      def image = docker.image(testImageName)
      image.push('test-tag')
      assert image.tagname == 'test-tag'
    }

    stage('Test docker.image.run()') {
      def container = docker.image(testImageName).run()
      container.stop()
    }

    stage('Test docker.image.tag()') {
      def image = docker.image(testImageName)
      image.tag('test')
      assert image.tagname == 'test'
    }

    stage('Test docker.image.withRun()') {
      def image = docker.image(testImageName)
      image.withRun {
        assert image.id == testImageName
      }
    }
  }
}

return this
