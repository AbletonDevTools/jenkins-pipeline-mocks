package com.ableton


@SuppressWarnings('EmptyMethod')
@SuppressWarnings('UnusedMethodParameter')
class DockerMock {
  static class Container {
    String id

    Container(String id = 'mock-container') {
      this.id = id
    }

    def port() {
      // TODO: Is this return type correct?
      return '1234'
    }

    def stop() {}
  }

  static class Image {
    String id
    String tagname

    Image(String id) {
      this.id = id
      this.tagname = 'latest'
    }

    def imageName() {
      return id
    }

    def inside(String args = '', Closure body) {
      return body(new Container())
    }

    def pull() {}

    def push(String tagname = '') {
      if (tagname) {
        tag(tagname)
      }
    }

    def run(String args = '', String command = '') {
      return new Container()
    }

    def tag(String tagname = '') {
      this.tagname = tagname
    }

    def withRun(String args = '', String command = '', Closure body) {
      return body(new Container())
    }
  }

  static build(String image, String args = '') {
    return new Image(image)
  }

  static image(String id) {
    return new Image(id)
  }

  static withRegistry(String url, String credentialsId = '', Closure body) {
    return body()
  }

  static withServer(String uri, String credentialsId = '', Closure body) {
    return body()
  }

  static withTool(String toolName, Closure body) {
    return body()
  }
}
