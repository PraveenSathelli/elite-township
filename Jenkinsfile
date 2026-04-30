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
        REG  = "image-registry.openshift-image-registry.svc:5000"
        APP  = "elite-app"
        IMG  = "elite-app"
        TAG = "${env.BUILD_NUMBER}"
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
              retry(2) {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn verify sonar:sonar \
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
        }
stage('Package JAR') {
    steps {
        sh '''
          mvn package -DskipTests \
          -Dmaven.repo.local=/var/jenkins_home/.m2/repository
        '''
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

                       def buildInfo = rtMaven.run(
                           pom: 'pom.xml',
                           goals: 'deploy -DskipTests'
                       )

                       // ✅ set build info correctly
                       buildInfo.name = "${APP}"
                       buildInfo.number = "${env.BUILD_NUMBER}"

                       // ✅ capture environment variables
                       buildInfo.env.capture = true

                       // ✅ attach Git info
                       buildInfo.vcs = [
                           url: "https://github.com/PraveenSathelli/elite-township.git",
                           revision: env.GIT_COMMIT,
                           branch: env.BRANCH_NAME
                       ]

                       // ✅ publish build info
                       server.publishBuildInfo(buildInfo)

                       // ✅ retention (optional)
                       server.discardBuilds([
                           buildName: "${APP}",
                           maxBuilds: 20,
                           deleteArtifacts: true
                       ])
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
                  oc version --client
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

        stage('Apply Openshift Build') {
            steps {
                sh '''
                  export PATH=$PATH:/tmp
                   oc process -f manifests/deployment/updated/bc.yaml \
                             -p APP=$APP -p IMG=$IMG -p TAG=$TAG -p PROJ=$PROJ | oc apply -f -

                   oc process -f manifests/deployment/updated/dc.yaml \
                             -p APP=$APP -p REG=$REG -p IMG=$IMG -p TAG=$TAG -p PROJ=$PROJ | oc apply -f -

                   oc process -f manifests/deployment/updated/config.yaml \
                             -p APP=$APP -p PROJ=$PROJ | oc apply -f -
                '''
            }
        }

        stage('Build Image from JAR') {
            steps {
                sh '''
                  export PATH=$PATH:/tmp
                  JAR_FILE=$(ls target/*.jar | head -n 1)
                  oc start-build $APP --from-file=$JAR_FILE --follow
                '''
            }
        }

        stage('Trigger Deployment') {
            steps {
                sh '''
                  export PATH=$PATH:/tmp
                   oc set image deployment/$APP $APP=$REG/$PROJ/$IMG:$TAG
                   oc rollout status deployment/$APP
                '''
            }
        }
    }
}