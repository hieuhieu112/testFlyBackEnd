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
        timeout(time: 30, unit: 'MINUTES')

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
    String commitMessage = sh(
        script: 'git log -1 --pretty="%s"',
        returnStdout: true
    ).trim()

    // Gom khoảng trắng và xuống dòng thành một khoảng trắng
    String normalizedMessage = commitMessage
        .replaceAll(/\s+/, ' ')
        .trim()

    // Cắt tối đa 36 ký tự, ưu tiên cắt tại khoảng trắng
    String displayMessage = normalizedMessage

    if (displayMessage.length() > 36) {
        displayMessage = displayMessage
            .substring(0, 36)
            .replaceFirst(/\s+\S*$/, '')
            .trim() + '...'
    }

    currentBuild.displayName =
        "#${env.BUILD_NUMBER} | ${displayMessage}"

    // Không hiện thêm dòng bên dưới card
    currentBuild.description = null
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
stage('Trivy vulnerability scan') {
    steps {
        sh '''
            set -eu

            mkdir -p trivy-reports

            ARTIFACT="$(find target \
                -maxdepth 1 \
                -type f \
                -name '*.jar' \
                | head -n 1)"

            test -n "$ARTIFACT"

            echo "=== Trivy scan target ==="
            echo "$ARTIFACT"

            trivy fs \
                --cache-dir /home/jenkins/agent/.trivy-cache \
                --scanners vuln \
                --offline-scan \
                --disable-telemetry \
                --skip-version-check \
                --skip-vex-repo-update \
                --parallel 1 \
                --severity HIGH,CRITICAL \
                --exit-code 0 \
                --format table \
                --output trivy-reports/vulnerability-report.txt \
                "$ARTIFACT"

            echo "=== Trivy report ==="
            cat trivy-reports/vulnerability-report.txt
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
                    artifacts: 'target/*.jar,build-info.txt,trivy-reports/*.txt',
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
