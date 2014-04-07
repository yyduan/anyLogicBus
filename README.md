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
     
