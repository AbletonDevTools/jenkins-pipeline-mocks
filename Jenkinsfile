@Library(['ableton-utils@0.3.0', 'groovylint@0.1.1']) _


runTheBuilds.runDevToolsProject(script: this,
  test: {
    parallel(failFast: false,
      groovylint: {
        groovylint.check('./Jenkinsfile')
      },
      junit: {
        try {
          sh './gradlew test'
        } finally {
          junit 'build/test-results/**/*.xml'
        }
      },
    )
  },
)
