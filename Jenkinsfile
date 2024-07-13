pipeline {
    agent any
    tools {
        nodejs "node"
    }
    
    environment {
        GIT_CREDENTIALS_ID = '0f0a50a3-ccfe-464e-9687-2fc6e2c3c235'
        MAIN_DOCKER_IMAGE = 'nodemain:v1.0'
        DEV_DOCKER_IMAGE = 'nodedev:v1.0'
        MAIN_CONTAINER_NAME = 'main_app'
        DEV_CONTAINER_NAME = 'dev_app'
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
                    def imageName = env.BRANCH_NAME == 'main' ? env.MAIN_DOCKER_IMAGE : env.DEV_DOCKER_IMAGE
                    sh "docker build -t ${imageName} ."
                }
            }
        }
         stage('Deploy') {
            steps {
                script {
                    def containerName = env.BRANCH_NAME == 'main' ? env.MAIN_CONTAINER_NAME : env.DEV_CONTAINER_NAME
                    def containerPort = env.BRANCH_NAME == 'main' ? '3000' : '3001'
                    def imageName = env.BRANCH_NAME == 'main' ? env.MAIN_DOCKER_IMAGE : env.DEV_DOCKER_IMAGE
                    sh """
                        docker stop ${containerName} || true
                        docker rm ${containerName} || true
                        docker run -d --name ${containerName} --expose ${containerPort} -p ${containerPort}:3000 ${imageName}
                        docker image prune -f
                    """
                }
            }
        }
    }
}
