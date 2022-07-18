@Echo OFF
SetLocal EnableDelayedExpansion

REM : Copyright (C) 2022 NeonOrbit
REM :
REM : Licensed under the Apache License, Version 2.0 (the "License");
REM : you may not use this file except in compliance with the License.
REM : You may obtain a copy of the License at
REM :
REM :     http://www.apache.org/licenses/LICENSE-2.0
REM :
REM : Unless required by applicable law or agreed to in writing, software
REM : distributed under the License is distributed on an "AS IS" BASIS,
REM : WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM : See the License for the specific language governing permissions and
REM : limitations under the License.

Java --version 2>nul 1>&2 || Call :Error "Please install JDK"

Set MAX_MEM=-Xmx4G
Set VERSION=0.0.0
Set PROGRAM=Dexplore

Set Executable=%PROGRAM%.jar
If Not Exist %Executable% (
    Call :SetLatestVersion 2>nul
    Set Executable=%PROGRAM%-!VERSION!.jar
    If Not Exist !Executable! Call :Error "Couldn't find %PROGRAM% jar"
)

Set "HELP=;;-h;--help;"
If "!HELP:;%~1;=!" == "!HELP!" echo Executing: %Executable% && echo.

Java %MAX_MEM% -jar -Duser.language=en -Dfile.encoding=UTF8 "%Executable%" %*
Goto Finish

:SetLatestVersion
    For /f "tokens=2 delims=-" %%V In (
        'Dir /b /a-d %PROGRAM%-*.jar'
    ) Do Set "X=%%~V" & If !X:~-3!==jar If %%~V gtr !VERSION! (
        Set VERSION=%%~nV
    )
Goto :EOF

:Error
Echo.
Echo  ERROR: %~1
Echo.

:Finish
Echo %CMDCMDLINE% | Find /i "/c" 2>nul 1>&2
If Not ERRORLEVEL 1 Pause
