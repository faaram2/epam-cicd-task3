pipeline {
    agent any
    tools {
        nodejs "node"
    }
    environment {
        GIT_CREDENTIALS_ID = '0f0a50a3-ccfe-464e-9687-2fc6e2c3c235'
        MAIN_DOCKER_IMAGE = 'nodemain:v1.0'
        DEV_DOCKER_IMAGE = 'nodedev:v1.0'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/${BRANCH_NAME}']],
                          userRemoteConfigs: [[credentialsId: "${env.GIT_CREDENTIALS_ID}", url: 'https://github.com/faaram2/epam-cicd-task3.git']]])
            }
        }
        stage('Build') {
            steps {
                sh 'npm install'
            }
        }
        stage('Test') {
            steps {
                sh 'npm test'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    def bla = env.BRANCH_NAME
                    def imageName = env.BRANCH_NAME == 'main' ? env.MAIN_DOCKER_IMAGE : env.DEV_DOCKER_IMAGE
                    sh "echo ${bla}   ${imageName}"
                    sh "docker build -t ${imageName} ."
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    def containerPort = env.BRANCH_NAME == 'main' ? '3000' : '3001'
                    def imageName = env.BRANCH_NAME == 'main' ? env.MAIN_DOCKER_IMAGE : env.DEV_DOCKER_IMAGE
                    def bla = env.BRANCH_NAME
                    sh '''
                        echo ">>${bla}<<  >>${imageName}<<  >>${containerPort}<<"
                        env
                        docker ps -q --filter "ancestor=${imageName}" | xargs -r docker stop
                        docker ps -a -q --filter "ancestor=${imageName}" | xargs -r docker rm
                        docker run -d --expose ${containerPort} -p ${containerPort}:3000 ${imageName}
                    '''
                }
            }
        }
    }
}
