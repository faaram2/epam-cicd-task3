def call(Map config = [:]) {
    pipeline {
        agent {
            docker {
                image 'custom-node-tools2:7.8.0'
                args ' -u root:root -v /var/run/docker.sock:/var/run/docker.sock'
            }
        }
        //The below block should be used in case if agent is not a docker
        //tools {
        //   nodejs "node"
        //}
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
            stage('Lint Dockerfile') {
                steps {
                    script {
                        sh 'hadolint Dockerfile'
                    }
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
            stage('Scan Docker Image for Vulnerabilities') {
                steps {
                    script {
                        def imageName = env.BRANCH_NAME == 'main' ? env.MAIN_DOCKER_IMAGE : env.DEV_DOCKER_IMAGE
                        def vulnerabilities = sh(script: "trivy image --exit-code 0 --severity HIGH,MEDIUM,LOW --no-progress ${imageName}", returnStdout: true).trim()
                        echo "Vulnerability Report:\n${vulnerabilities}"
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
                        build job: 'Deploy_to_main', wait: false
                    } else if (env.BRANCH_NAME == 'dev') {
                        build job: 'Deploy_to_dev', wait: false
                    }
                }
            }
        }
    }
}
