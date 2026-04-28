pipeline {
  agent {
    kubernetes {
      label 'maven-oc-agent'
      defaultContainer 'maven'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9.9-eclipse-temurin-17
    command: ['cat']
    tty: true

  - name: oc
    image: quay.io/openshift/origin-cli
    command: ['cat']
    tty: true
"""
    }
  }

  environment {
    APP  = "elite-app"
    IMG  = "elite-app"
    TAG  = "latest"
    PROJ = "praveensathelli11-dev"   // change to your namespace
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

    stage('Build') {
      steps {
        container('maven') {
          sh 'mvn clean compile -Dmaven.repo.local=/home/jenkins/.m2/repository'
        }
      }
    }

    stage('Unit Test') {
      steps {
        container('maven') {
          sh 'mvn test -Dmaven.repo.local=/home/jenkins/.m2/repository'
        }
      }
    }

    stage('Sonar Analysis') {
      steps {
        container('maven') {
          withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
            sh '''
              mvn clean verify sonar:sonar \
              -Dsonar.host.url=http://sonarqube-praveensathelli11-dev.apps.rm1.0a51.p1.openshiftapps.com \
              -Dsonar.login=$SONAR_TOKEN \
              -Dsonar.projectName=eliteTownship \
              -Dsonar.projectKey=eliteTownship \
              -Dmaven.repo.local=/home/jenkins/.m2/repository
            '''
          }
        }
      }
    }

    stage('Publish to Artifactory') {
      steps {
        container('maven') {
          script {
            def server = Artifactory.server('jfrog-config')

            def rtMaven = Artifactory.newMavenBuild()
//             rtMaven.tool = 'Maven-3'

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

    // ✅ Apply OpenShift templates (one-time safe, re-runnable)
    stage('Apply OpenShift Config') {
      steps {
        container('oc') {
          withCredentials([string(credentialsId: 'oc-token', variable: 'OPENSHIFT_TOKEN')]) {
            sh '''
              oc login  --token=$OPENSHIFT_TOKEN \
                --server=https://api.rm1.0a51.p1.openshiftapps.com:6443

              oc project $PROJ

              oc process -f manifests/deployment/updated/bc.yaml \
                -p APP=$APP -p IMG=$IMG -p TAG=$TAG -p PROJ=$PROJ | oc apply -f -

              oc process -f manifests/deployment/updated/dc.yaml \
                -p APP=$APP -p IMG=$IMG -p TAG=$TAG -p PROJ=$PROJ | oc apply -f -

              oc process -f manifests/deployment/updated/config.yaml \
                -p APP=$APP -p PROJ=$PROJ | oc apply -f -
            '''
          }
        }
      }
    }

    // ✅ Build image from JAR
    stage('Build Image') {
      steps {
        container('oc') {
          sh '''
            oc start-build $APP --from-file=target/*.jar --follow
          '''
        }
      }
    }

    // ✅ Verify deployment
    stage('Verify Deployment') {
      steps {
        container('oc') {
          sh '''
            oc get pods
            oc get svc
            oc get route
          '''
        }
      }
    }
  }
}