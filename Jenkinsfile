pipeline {
    agent any

    tools {
        maven 'Maven-3'
        
    }

    stages {

        stage('Git Checkout') {
            steps {
                git changelog: false,
                    credentialsId: 'bed3ccd3-a099-4fcc-9aeb-2d953cc24ac8',
                    poll: false,
                    url: 'https://github.com/PraveenSathelli/elite-township.git'
            }
        }

        stage('Build & Sonar Analysis') {
            steps {
               withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn clean verify sonar:sonar \
                        -Dsonar.host.url=http://sonarqube-praveensathelli11-dev.apps.rm1.0a51.p1.openshiftapps.com \
                        -Dsonar.login=$SONAR_TOKEN \
                        -Dsonar.projectName=eliteTownship \
                        -Dsonar.java.binaries=. \
                        -Dsonar.projectKey=eliteTownship \
                        -Dmaven.repo.local=/var/jenkins_home/.m2/repository \
                        -Dsonar.userHome=/var/jenkins_home/.sonar
                    '''
               }
            }
        }
         stage('Publish to Artifactory') {
                    steps {
                        script {
                            def server = Artifactory.server('jfrog-cred')

                            def rtMaven = Artifactory.newMavenBuild()
                            rtMaven.tool = 'Maven-3'

                            rtMaven.deployer(
                                releaseRepo: 'libs-release-local',
                                snapshotRepo: 'libs-snapshot-local',
                                server: server
                            )

                            rtMaven.resolver(
                                releaseRepo: 'libs-release',
                                snapshotRepo: 'libs-snapshot',
                                server: server
                            )

                            rtMaven.run pom: 'pom.xml', goals: 'clean deploy -DskipTests'
                        }
                    }
                }
    }
}