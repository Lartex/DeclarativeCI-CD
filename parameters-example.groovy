   parameters {
        string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
        text(name: 'BIOGRAPHY', defaultValue: '', description: 'Enter some information about the person')
        booleanParam(name: 'TOGGLE', defaultValue: true, description: 'Toggle this value')
        choice(name: 'CHOICE', choices: ['Nexus', 'Artifactory'], description: 'Pick something')
        password(name: 'PASSWORD', defaultValue: 'SECRET', description: 'Enter a password')
    }   
  
  stage('Check Parameters'){
      steps {
        echo "PERSON: ${PERSON}"
        echo "BIOGRAPHY: ${BIOGRAPHY}"
        echo "TOGGLE: ${TOGGLE}"
        echo "CHOICE: ${CHOICE}"
        echo "PASSWORD: ${PASSWORD}"

        script{
          if(CHOICE == 'One'){
            echo 'Parameters OK'
          }else{
            echo 'Parameters FAILURE'
             error('Stopping early…')
          }
        }
      }
    }