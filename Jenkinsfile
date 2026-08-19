pipeline {
    agent {
        label 'ubuntu-qap'
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

        stage('Archive artifact') {
            steps {
                archiveArtifacts(
                    artifacts: 'target/*.jar',
                    fingerprint: true,
                    onlyIfSuccessful: true
                )
            }
        }
    }

    post {
        success {
            echo 'CHECKOUT + BUILD + ARCHIVE: SUCCESS'
        }

        failure {
            echo 'PIPELINE: FAILED'
        }

        aborted {
            echo 'PIPELINE: ABORTED'
        }
    }
}
