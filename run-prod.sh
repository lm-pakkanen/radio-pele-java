sudo killall -9 java || true && mvn clean package -Dmaven.test.skip=true && java -jar -noverify target/radio_pele_java-0.0.1-SNAPSHOT.jar &
