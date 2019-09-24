java -Xmx1024m -jar molachat.jar \
--server.port=8088 \
--self-conf.connect-timeout=20000 \
--self-conf.close-timeout=180000 \
--self-conf.max-client-num=50 \
--self-conf.max-session-message-num=100 \
--self-conf.upload-file-path=/tmp/chattmp \
--self-conf.max-file-size=1000 \
--self-conf.max-request-size=1000 \
&
