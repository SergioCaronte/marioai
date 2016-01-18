@echo off

:: This script should be runned from Microsoft Visual Studio Command Prompt

SET build_dir=..\..\..\..\bin\AmiCoBuild\JavaPy
SET src_dir=src

if "%1" == "" goto :build
if "%1" == "clean" goto :clean
goto :eof

:build
if not exist "%~dp0\%build_dir%" mkdir "%~dp0\%build_dir%"
echo.
echo -------------------
echo Compiling......
echo The result will be stored in "bin\AmiCoBuild\JavaPy" directory
echo -------------------
echo.
cl /EHsc ".\%src_dir%\ch_idsia_tools_amico_AmiCoJavaPy.cc" /IC:\Python27\include /I"C:\Program Files\Java\jdk1.8.0_60\include" /I"C:\Program Files\Java\jdk1.8.0_60\include\win32" "C:\Python27\libs\python27.lib" "C:\Program Files\Java\jdk1.8.0_60\lib\jvm.lib" /LD /Fe"%build_dir%\AmiCoJavaPy.dll" /Fo"%build_dir%\AmiCoJavaPy.obj"  /DEF:".\"%src_dir%"\AmiCoJavaPy.def"
goto :eof

:clean
del /F /Q "%build_dir%"
goto :eof

:missing
echo C/C++ compiler not found in the system path. Try to run this script from Microsoft Visual Studio Command Prompt.
goto :eof