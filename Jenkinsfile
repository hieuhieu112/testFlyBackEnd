pipeline {
    agent {
        label 'ubuntu-qap'
    }

    triggers {
        pollSCM('H/2 * * * *')
    }

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timeout(time: 15, unit: 'MINUTES')

        buildDiscarder(
            logRotator(
                numToKeepStr: '10',
                artifactNumToKeepStr: '5'
            )
        )

        copyArtifactPermission('testFlyBackEnd-deploy')
    }

    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                checkout scm
            }
        }

        stage('Inspect project') {
    steps {
        sh '''
            set -eu

            echo "=== Git information ==="
            git remote -v
            git rev-parse --short HEAD
            git log -1 --oneline

            echo "=== Project information ==="
            test -f pom.xml
            grep -nE "java.version|spring-boot" pom.xml || true
        '''

        script {
    String shortCommit = sh(
        script: 'git rev-parse --short HEAD',
        returnStdout: true
    ).trim()

    String commitMessage = sh(
        script: 'git log -1 --pretty="%s"',
        returnStdout: true
    ).trim()

    String commitAuthor = sh(
        script: 'git log -1 --pretty="%an"',
        returnStdout: true
    ).trim()

    String shortMessage = commitMessage
        .replaceAll(/[\\r\\n]+/, ' ')
        .take(60)

    currentBuild.displayName =
        "#${env.BUILD_NUMBER} - ${shortCommit} - ${shortMessage}"

    currentBuild.description =
        "${commitAuthor}: ${commitMessage}".take(120)
}
    }
}

        stage('Maven build') {
            steps {
                sh '''
                    set -eu

                    export JAVA_HOME=/opt/java/openjdk
                    export PATH="$JAVA_HOME/bin:/opt/maven/bin:$PATH"

                    echo "=== Build environment ==="
                    java -version
                    mvn --version

                    echo "=== Build application ==="
                    mvn --batch-mode --no-transfer-progress \
                        clean package \
                        -DskipTests

                    echo "=== Generated artifacts ==="
                    ls -lh target/*.jar
                '''
            }
        }

        stage('Create build metadata') {
            steps {
                sh '''
                    set -eu

                    ARTIFACT="$(find target -maxdepth 1 -type f -name '*.jar' | head -n 1)"
                    test -n "$ARTIFACT"

                    {
                        echo "SOURCE_JOB=$JOB_NAME"
                        echo "SOURCE_BUILD_NUMBER=$BUILD_NUMBER"
                        echo "SOURCE_BUILD_URL=$BUILD_URL"
                        echo "GIT_COMMIT=$(git rev-parse HEAD)"
                        echo "GIT_BRANCH=main"
                        echo "ARTIFACT=$(basename "$ARTIFACT")"
                        echo "SHA256=$(sha256sum "$ARTIFACT" | awk '{print $1}')"
                        echo "BUILT_AT=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
                    } > build-info.txt

                    cat build-info.txt
                '''
            }
        }

        stage('Archive artifact') {
            steps {
                archiveArtifacts(
                    artifacts: 'target/*.jar,build-info.txt',
                    fingerprint: true,
                    onlyIfSuccessful: true
                )
            }
        }
    }

    post {
        success {
            echo 'CI BUILD: SUCCESS'
        }

        failure {
            echo 'CI BUILD: FAILED'
        }

        aborted {
            echo 'CI BUILD: ABORTED'
        }
    }
}
