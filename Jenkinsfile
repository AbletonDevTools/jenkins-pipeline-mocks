library 'ableton-utils@0.13'
library 'groovylint@0.4'


devToolsProject.run(
  test: {
    parallel(failFast: false,
      groovylint: {
        groovylint.check('./Jenkinsfile,./*.gradle,**/*.groovy')
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
  deployWhen: { return runTheBuilds.isPushTo(['master']) },
  deploy: {
    String versionNumber = readFile('VERSION').trim()
    version.tag(versionNumber)
    version.forwardMinorBranch(versionNumber)
  },
)
