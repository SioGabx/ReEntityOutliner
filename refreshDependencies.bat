rmdir .gradle /s /q

./gradlew clean build --refresh-dependencies
./gradlew genSources
./gradlew idea
pause