rem 13.114.190.10 dev
del /s /q ap
rd /s /q ap
timeout /T 1
call npm run build
timeout /T 2
ren D:\sourceCode\phpProject\phppay\phpForntend\phpadmin\dist ap

pscp -pw Pc13142413179  -r D:\sourceCode\phpProject\phppay\phpForntend\phpadmin\ap root@%1:/data/www/