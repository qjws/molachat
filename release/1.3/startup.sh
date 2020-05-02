java -Xmx1024m -jar molachat.jar \
--server.port=8550 \
--self-conf.connect-timeout=60000 \
--self-conf.close-timeout=3600000 \
--self-conf.max-client-num=100 \
--self-conf.max-session-message-num=100 \
--self-conf.upload-file-path=/tmp/chattmp \
--self-conf.max-file-size=1000 \
--self-conf.max-request-size=1000 \
&
