#!/bin/sh  
  
SERVER=/home/mini_memcached 
cd $SERVER  
  
case "$1" in  
  
  start)  
    nohup java -Xmx128m -jar mini-memcached.jar > $SERVER/server.log 2>&1 &  
    echo $! > $SERVER/server.pid
    echo "mini-memecached server started"  
    ;;  
  
  stop)  
    kill `cat $SERVER/server.pid`  
    rm -rf $SERVER/server.pid  
    ;;  
  
  restart)  
    $0 stop  
    sleep 1  
    $0 start  
    ;;  
  
  *)  
    echo "Usage: run.sh {start|stop|restart}"  
    ;;  
  
esac  
  
exit 0  
