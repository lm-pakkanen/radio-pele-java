set -e
sudo killall -9 java || true &&
  ./mvnw clean package -Dmaven.test.skip=true &&
  java -jar -noverify target/radio_pele_java-0.0.1-SNAPSHOT.jar
