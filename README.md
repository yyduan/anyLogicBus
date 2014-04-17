anyLogicBus readme
==================

一个高效的轻量级服务框架

### 特征
anyLogicBus是一个高效的,可扩展的,轻量级服务框架,具有下列特征:
 
 - 服务路由，将HTTP请求分发给具体服务模块；
 
 - 访问控制，支持多种访问控制策略，并且可定制；
 
 - 插件式服务模块，提供服务规范，支持定制开发
 
 - 可定制的服务目录，支持多种形式的服务目录
 
 - 内置多种管理服务
 
 - 可部署成集群管理模式，分为元数据服务器，监控服务器，服务节点进行部署
 
 - 基于anyWebLoader自动更新框架
 
### 版本
 
 - 1.0.0 [20140327 duanyy]
     + 首次发布
     
 - 1.0.1 [20140402 duanyy]
     + 改进访问控制模型，以避免SessionID多次计算
     
 - 1.0.2 [20140407 duanyy]
     + 修改{@link com.logicbus.backend.server.MessageRouter MessageRouter},
     采用{@link java.util.concurrent.CountDownLatch CountDownLatch}来等待服务执行。
     
 - 1.0.3 [20140410 duanyy]
     + 在{@link com.logicbus.models.servant.ServiceDescription ServiceDescription}中增加调用参数列表
     + 在{@link com.logicbus.backend.Servant Servant}增加调用参数读取的封装函数 
 
 - 1.0.4 [20140410 duanyy]
     + 增加Raw消息，见{@link com.logicbus.backend.message.RawMessage RawMessage}
     + 增加客户端调用框架，见{@link com.logicbus.client Client}
     
 - 1.0.5 [20140412 duanyy]
     + 改进消息传递模型
     + 增加开发文档(development.md)，并增加一些案例(com.logicbus.examples)
     
 - 1.0.6 [20140417 duanyy]
     + 增加数据库连接池(com.logicbus.datasource)
     