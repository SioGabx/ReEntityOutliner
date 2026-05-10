rmdir .gradle /s /q
.\gradlew clean build --refresh-dependencies -Dorg.gradle.java.home="C:\Program Files\Java\jdk-17.0.4.1"
./gradlew genSources
./gradlew idea
pause