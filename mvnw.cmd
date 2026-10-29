@ECHO OFF
SETLOCAL

IF EXIST "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" (
    CALL "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" %*
    EXIT /B %ERRORLEVEL%
)

IF EXIST "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\56ba1f9f\bin\mvn.cmd" (
    CALL "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\56ba1f9f\bin\mvn.cmd" %*
    EXIT /B %ERRORLEVEL%
)

mvn %*
EXIT /B %ERRORLEVEL%

