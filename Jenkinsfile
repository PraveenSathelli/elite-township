pipeline {
    agent any

 options {
        skipDefaultCheckout(true)
    }

    tools {
        maven 'Maven-3'
    }

    environment {
        OC_VERSION = "latest"
        OC_HOME = "/tmp"
        KUBECONFIG = "/tmp/kubeconfig"

        APP  = "elite-app"
        IMG  = "elite-app"
        TAG  = "latest"
        PROJ = "praveensathelli11-dev"
    }

    stages {

        stage('Git Checkout') {
            steps {
//                 git changelog: false,
//                     credentialsId: 'bed3ccd3-a099-4fcc-9aeb-2d953cc24ac8',
//                     poll: false,
//                     url: 'https://github.com/PraveenSathelli/elite-township.git'
                     script {
                      def branch = env.BRANCH_NAME ?: 'master'

                      echo "Building branch: ${branch}"

                      git branch: branch,
                          credentialsId: 'bed3ccd3-a099-4fcc-9aeb-2d953cc24ac8',
                          url: 'https://github.com/PraveenSathelli/elite-township.git'
                  }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -Dmaven.repo.local=/var/jenkins_home/.m2/repository'
            }
        }

        stage('Unit Test') {
            steps {
                sh 'mvn test -Dmaven.repo.local=/var/jenkins_home/.m2/repository'
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
                                   def server = Artifactory.server('jfrog-config')

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

                                withEnv(["MAVEN_OPTS=-Dmaven.repo.local=/var/jenkins_home/.m2/repository"]) {
                                               rtMaven.run pom: 'pom.xml', goals: 'clean deploy -DskipTests'
                                           }
                               }
                           }
                       }

                       
        stage('Install oc CLI') {
            steps {
                sh '''
                  curl -LO https://mirror.openshift.com/pub/openshift-v4/clients/oc/${OC_VERSION}/linux/oc.tar.gz
                  tar -xvf oc.tar.gz
                  chmod +x oc kubectl
                  mv oc kubectl /tmp/

                  export PATH=$PATH:/tmp
                  oc version
                '''
            }
        }

        stage('Login to OpenShift') {
            steps {
                withCredentials([string(credentialsId: 'oc-token', variable: 'OPENSHIFT_TOKEN')]) {
                    sh '''
                      export PATH=$PATH:/tmp
                      touch $KUBECONFIG

                      oc login --token=$OPENSHIFT_TOKEN \
                        --server=https://api.rm1.0a51.p1.openshiftapps.com:6443

                      oc project $PROJ
                    '''
                }
            }
        }

        stage('Apply BuildConfig') {
            steps {
                sh '''
                  export PATH=$PATH:/tmp
                  oc process -f manifests/deployment/updated/bc.yaml \
                    -p APP=$APP -p IMG=$IMG -p TAG=$TAG -p PROJ=$PROJ | oc apply -f -
                '''
            }
        }

        stage('Apply DeploymentConfig') {
            steps {
                sh '''
                  export PATH=$PATH:/tmp
                  oc process -f manifests/deployment/updated/dc.yaml \
                    -p APP=$APP -p IMG=$IMG -p TAG=$TAG -p PROJ=$PROJ | oc apply -f -
                '''
            }
        }

        stage('Apply Service & Route') {
            steps {
                sh '''
                  export PATH=$PATH:/tmp
                  oc process -f manifests/deployment/updated/config.yaml \
                    -p APP=$APP -p PROJ=$PROJ | oc apply -f -
                '''
            }
        }

        stage('Build Image from JAR') {
            steps {
                sh '''
                  export PATH=$PATH:/tmp
                  oc start-build $APP --from-file=target/*.jar --follow
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                  export PATH=$PATH:/tmp
                  oc get pods
                  oc get svc
                  oc get route
                '''
            }
        }
    }
}