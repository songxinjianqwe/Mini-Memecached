# 简化版memcached服务器
## 实现功能
- 支持get、set、delete指令
- 支持惰性缓存过期策略

## 设计思路
基于Java的NIO实现IO多路复用，Reactor模型，由一个Acceptor线程来循环检测客户端的连接事件与读就绪事件。
当Acceptor接收到客户端的读就绪事件时，会将客户端连接句柄交给RequestDispatcher中，它持有一个业务线程池，
该线程池会调用RequestHandler，它将拿到客户端连接，进行读取，然后将指令交给CommandExecutor去执行指令。
CommandExecutor会获得单例CacheManager的引用，然后遍历所有的Command实例，基于责任链模式去检测该Command是否
支持该指令，若支持，则执行，然后返回结果。将结果交给RequestHandler，将结果写入到客户端连接中。


- {basePackage}/Bootstrap 
    - Main方法所在类
- {basePackage}/server/Server 
    - 服务器主体，其内部类Acceptor会循环检测客户端的就绪事件
- {basePackage}/request/RequestDispatcher 
    - 持有一个业务线程池，它会将客户端的请求放入至线程池中
- {basePackage}/request/RequestHandler 
    - 线程池执行的runnable，它会进行IO读取指令，然后交给CommandExecutor去执行指令
- {basePackage}/command/CommandExecutor
    - 指令执行器，持有所有具体的Command，组成一个链，如果某个Command支持该指令，则调用该Command执行指令
- {basePackage}/cache/CacheManager
    - 缓存管理器，单例，基于ConcurrentHashMap，支持线程安全的插入、删除等  
    

## 运行示例
要求将mini_memcached.jar放在/home/mini_memcached目录下
然后执行 ./run.sh start ，如果启动成功，将显示mini-memecached server started
该服务器会监听在8888端口上，客户端使用telnet localhost 8888来连接服务器。

测试用例1
![test_case1](http://markdown-1252651195.cossh.myqcloud.com/%E6%B5%8B%E8%AF%95%E7%94%A8%E4%BE%8B.png)

cd /home/memcached
./run.sh start

> telnet localhost 8888
> set k1 0 0 2
> v1
< STORED
> get k1
< VALUE k1 0 2
< v1
< END
> delete 1
< DELETED
> get k1
< END
> quit
< Connection closed by foreign host

./run.sh stop

测试用例2
![test_cast2](http://markdown-1252651195.cossh.myqcloud.com/%E6%B5%8B%E8%AF%95%E7%94%A8%E4%BE%8B2.png)

cd /home/memcached
./run.sh start

> telnet localhsot 8888
> get k1
< END
> set k1 0 10 2
> v1
< STORED
> get k1
< VALUE 1 0 2
< v1
< END
> get k1 (此时已经过期)
< END
> delete k2
< NOT_FOUND
> quit
< Connection closed by foreign host

./run.sh stop
