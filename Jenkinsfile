@Library([
  'ableton-utils@0.8',
  'groovylint@0.4',
]) _


runTheBuilds.runDevToolsProject(
  test: {
    parallel(failFast: false,
      groovylint: {
        groovylint.check('./Jenkinsfile,./src/**.groovy,*.gradle')
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
  deploy: {
    runTheBuilds.runForSpecificBranches(['master'], false) {
      String versionNumber = readFile('VERSION').trim()
      version.tag(versionNumber)
      version.forwardMinorBranch(versionNumber)
    }
  },
)
