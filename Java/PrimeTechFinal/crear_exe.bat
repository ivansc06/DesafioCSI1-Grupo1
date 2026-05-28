@echo off
setlocal
echo ============================================
echo  Generando PrimeTechSystems.exe
echo ============================================

set PROYECTO=C:\Users\dpjos\Documents\Desafio-Tienda\DesafioCSI1-Grupo1\Java\PrimeTechFinal
set LIBS=C:\Users\dpjos\Documents\Desafio-Tienda\DesafioCSI1-Grupo1\Java\librerias
set TEMP_DIR=%PROYECTO%\build\fatjar_tmp
set DIST_DIR=%PROYECTO%\dist
set OUTPUT_DIR=%PROYECTO%\instalador

REM ---- 1. Limpiar temporales ----
echo [1/5] Limpiando temporales...
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
if exist "%DIST_DIR%"  rmdir /s /q "%DIST_DIR%"
mkdir "%TEMP_DIR%"
mkdir "%DIST_DIR%"

REM ---- 2. Extraer todas las dependencias ----
echo [2/5] Extrayendo dependencias...
cd /d "%TEMP_DIR%"

jar xf "%LIBS%\AbsoluteLayout.jar"
jar xf "%LIBS%\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar"
jar xf "%LIBS%\apache-log4j-2.25.4-bin\log4j-api-2.25.4.jar"
jar xf "%LIBS%\apache-log4j-2.25.4-bin\log4j-core-2.25.4.jar"
jar xf "%LIBS%\log4j-slf4j2-impl-2.25.4.jar"
jar xf "%LIBS%\slf4j-api-2.0.17.jar"
jar xf "%LIBS%\HikariCP-4.0.3.jar"
jar xf "%LIBS%\flatlaf-3.4.jar"
jar xf "%LIBS%\poi-5.3.0.jar"
jar xf "%LIBS%\poi-ooxml-5.3.0.jar"
jar xf "%LIBS%\poi-ooxml-full-5.3.0.jar"
jar xf "%LIBS%\commons-compress-1.27.1.jar"
jar xf "%LIBS%\commons-collections4-4.4.jar"
jar xf "%LIBS%\commons-io-2.15.1.jar"
jar xf "%LIBS%\xmlbeans-5.2.1.jar"
jar xf "%LIBS%\jakarta.mail-2.0.1.jar"
jar xf "%LIBS%\jakarta.activation-2.0.1.jar"
jar xf "%LIBS%\jfreechart-1.5.4.jar"
jar xf "%LIBS%\core-3.5.3.jar"
jar xf "%LIBS%\javase-3.5.3.jar"

REM ---- 3. Copiar las clases del proyecto encima ----
echo [3/5] Copiando clases del proyecto...
xcopy /s /y "%PROYECTO%\build\classes\*" "%TEMP_DIR%\" >nul

REM ---- 4. Crear el fat JAR ----
echo [4/5] Empaquetando fat JAR...
echo Main-Class: primetechfinal.PrimeTech> "%TEMP_DIR%\MANIFEST.MF"
echo.>> "%TEMP_DIR%\MANIFEST.MF"

cd /d "%TEMP_DIR%"
jar cfm "%DIST_DIR%\PrimeTechFinal.jar" "%TEMP_DIR%\MANIFEST.MF" .

echo FAT JAR generado:
dir "%DIST_DIR%\PrimeTechFinal.jar"

REM ---- 5. Generar el .exe con jpackage ----
echo [5/5] Generando .exe con jpackage...
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"

jpackage ^
  --input "%DIST_DIR%" ^
  --name PrimeTechSystems ^
  --main-jar PrimeTechFinal.jar ^
  --main-class primetechfinal.PrimeTech ^
  --type app-image ^
  --dest "%OUTPUT_DIR%" ^
  --app-version 1.0 ^
  --vendor "PrimeTech Systems" ^
  --java-options "-Dfile.encoding=UTF-8"

echo.
echo ============================================
if exist "%OUTPUT_DIR%\PrimeTechSystems\PrimeTechSystems.exe" (
    echo  EXITO^^! EXE generado en:
    echo  %OUTPUT_DIR%\PrimeTechSystems\PrimeTechSystems.exe
) else (
    echo  ERROR: No se genero el EXE. Revisa los mensajes anteriores.
)
echo ============================================

rmdir /s /q "%TEMP_DIR%"
pause
