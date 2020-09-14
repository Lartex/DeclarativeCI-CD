def server = Artifactory.server 'jenkins-artifactory-server'
def rtMaven = Artifactory.newMavenBuild()
def buildInfo

pipeline {
  agent { label 'master' }
  tools {
    maven 'MAVEN_LATEST'
    jdk 'JAVA_HOME'
  }
  parameters {
    string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
    text(name: 'BIOGRAPHY', defaultValue: '', description: 'Enter some information about the person')
    booleanParam(name: 'TOGGLE', defaultValue: true, description: 'Toggle this value')
    choice(name: 'REPOSITORY', choices: ['Nexus', 'Artifactory'], description: 'Pick something')
    password(name: 'PASSWORD', defaultValue: 'SECRET', description: 'Enter a password')
  }
  options {
    timestamps ()
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '10', numToKeepStr: '5')
    // numToKeepStr - Max # of builds to keep
    // daysToKeepStr - Days to keep builds
    // artifactDaysToKeepStr - Days to keep artifacts
    // artifactNumToKeepStr - Max # of builds to keep with artifacts
  }
  environment {
    SONAR_HOME = "${tool name: 'sonarqube-scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'}"
    NEXUS_VERSION = 'nexus3'
    NEXUS_PROTOCOL = 'http'
    NEXUS_URL = '172.17.0.3:8081'
    NEXUS_REPOSITORY = 'nexus-jenkins-repo'
    NEXUS_CREDENTIAL_ID = 'nexus-credentials'
  }
  stages {
    stage('Echo Parameters') {
      steps {
        echo "PERSON ${PERSON}"
        echo "BIOGRAPHY ${BIOGRAPHY}"
        echo "TOGGLE ${TOGGLE}"
        echo "REPOSITORY ${REPOSITORY}"
        echo "PASSWORD ${PASSWORD}"
      }
    }
    stage('Execute Maven') {
      steps {
        script {
          rtMaven.run pom: 'pom.xml', goals: 'clean install'
        }
      }
    }
    stage('SonarQube Analysis') {
      steps {
        script {
          scannerHome = tool 'sonarqube-scanner'
        }
        withSonarQubeEnv('sonar') {
            sh """${scannerHome}/bin/sonar-scanner"""
        }
        //Quality Gate
        sleep(10)
        timeout(time: 1, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }
    stage('Publish to Repository Manager') {
          steps {
            script {
          if (REPOSITORY == 'Artifactory') {
            rtMaven.tool = 'MAVEN_LATEST'
            rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server
            buildInfo = Artifactory.newBuildInfo()
            rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot', server: server
            buildInfo.env.capture = true
             currentBuild.result = 'SUCCESS'
             echo "${buildInfo}"
             echo "${buildInfo.env.capture}"
          }
                  else {
            pom = readMavenPom file: 'pom.xml'
            filesByGlob = findFiles(glob: "target/*.${pom.packaging}")
            echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
            artifactPath = filesByGlob[0].path
            artifactExists = fileExists artifactPath
            if (artifactExists) {
              echo "*** File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}"
              nexusArtifactUploader(
                                    nexusVersion: NEXUS_VERSION,
                                    protocol: NEXUS_PROTOCOL,
                                    nexusUrl: NEXUS_URL,
                                    groupId: pom.groupId,
                                    version: pom.version,
                                    repository: NEXUS_REPOSITORY,
                                    credentialsId: NEXUS_CREDENTIAL_ID,
                                    artifacts: [
                                        [artifactId: pom.artifactId,
                                    classifier: '',
                                    file: artifactPath,
                                    type: pom.packaging],
                                    [artifactId: pom.artifactId,
                                    classifier: '',
                                    file: 'pom.xml',
                                    type: 'pom']
                                ]
                            )                         
                        }else {
              error "*** File: ${artifactPath}, could not be found"
            }
                  }
            }
          }
    }
    stage('Deleting Docker') {
      steps {
        sh 'chmod +x delete_cont.sh'
        sh './delete_cont.sh'
      }
    }
    stage('Build Docker') {
      steps {
        sh 'docker build -t lartex/springtest:$BUILD_NUMBER .'
      }
    }
    stage('Docker Push Registry') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker', passwordVariable: 'docker_pass', usernameVariable: 'docker_user')]) {
          sh 'docker login -u ${docker_user} -p ${docker_pass}'
            sh 'docker push lartex/springtest:$BUILD_NUMBER'
          sh 'docker run -d -p 8050:8050 --name SpringbootApp lartex/springtest:$BUILD_NUMBER'
        }
      }
    }
  }
    post {
        success {
      emailext body: "Project: ${env.JOB_NAME} Build Number: ${env.BUILD_NUMBER} URL: ${env.BUILD_URL}", recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']], subject: "Success: Project name -> ${env.JOB_NAME}", to: 'lartex7@gmail.com'
        }
        failure {
          sh 'echo "This will run only if failed"'
      emailext body: "Project: ${env.JOB_NAME} Build Number: ${env.BUILD_NUMBER} URL: ${env.BUILD_URL}", recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']], subject: "ERROR: Project name -> ${env.JOB_NAME}", to: 'lartex7@gmail.com'
        }
    }
}
