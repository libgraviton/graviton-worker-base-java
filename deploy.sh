mvn clean deploy
# second test run not needed, already done
mvn -DskipTests=true -Dmaven.test.skip=true -DuseGithub=true deploy
