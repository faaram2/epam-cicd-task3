pipeline {
    agent any
    tools {
        nodejs "node"
    }
    environment {
        GIT_CREDENTIALS_ID = '0f0a50a3-ccfe-464e-9687-2fc6e2c3c235'
        DOCKER_HUB_CREDENTIALS_ID = '43a4b6a6-4979-4f9a-946a-12200e5a5ba5'
        DOCKER_HUB_REPO_MAIN = 'faaram1/main_app'
        DOCKER_HUB_REPO_DEV = 'faaram1/dev_app'
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
                    def imageName = env.BRANCH_NAME == 'main' ? env.MAIN_DOCKER_IMAGE : env.DEV_DOCKER_IMAGE
                    sh "docker build -t ${imageName} ."
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    def imageName = env.BRANCH_NAME == 'main' ? env.MAIN_DOCKER_IMAGE : env.DEV_DOCKER_IMAGE
                    def dockerHubRepo = env.BRANCH_NAME == 'main' ? env.DOCKER_HUB_REPO_MAIN : env.DOCKER_HUB_REPO_DEV
                    def dockerHubTag = "${dockerHubRepo}:v1.0"
                    withCredentials([usernamePassword(credentialsId: "${env.DOCKER_HUB_CREDENTIALS_ID}", usernameVariable: 'DOCKER_HUB_USER', passwordVariable: 'DOCKER_HUB_PASS')]) {
                        sh """
                            echo "${DOCKER_HUB_PASS}" | docker login -u "${DOCKER_HUB_USER}" --password-stdin
                            docker tag ${imageName} ${dockerHubTag}
                            docker push ${dockerHubTag}
                        """
                    }
                }
            }
        }
    }
    post {
        success {
            script {
                if (env.BRANCH_NAME == 'main') {
                    build job: 'main-deployment-pipeline', wait: false
                } else if (env.BRANCH_NAME == 'dev') {
                    build job: 'dev-deployment-pipeline', wait: false
                }
            }
        }
    }
}
