@echo off
cd /d %~dp0
git reset --hard origin/dev-service
git status
pause