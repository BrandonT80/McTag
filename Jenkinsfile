node {
  stage('SCM') {
    checkout scm
  }
  stage('SonarQube Analysis') {
    def mvn = tool 'FableMaven';
    withSonarQubeEnv() {
      bat "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=BrandonT80_McTag_17ee441d-498b-4080-b347-87dd2e1ba788 -Dsonar.projectName='McTag'"
    }
  }
}