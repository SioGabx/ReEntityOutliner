# Définit la variable système JAVA_HOME
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-25.0.2", "Machine")

# Ajoute le dossier bin au début du Path système
$oldPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
$newPath = "%JAVA_HOME%\bin;" + $oldPath
[Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")