## 开始使用

### 一.环境配置
#### 1.在pom文件中加入仓库地址: (后续会有maven中央仓库版本发布)
```xml
<repositories>
    <repository>
        <id>gomyck-repo</id>
        <name>Gomyck proxy Ali</name>
        <url>http://nexus.gomyck.com/nexus/content/repositories/gomyck-repo/</url>
    </repository>
    
    <!-- 快照版本更新频繁, 如不想和作者保持同步, 请删除下面本仓库地址并使用release版本: 1.0.0-release -->
    <repository>
        <id>gomyck-repo-snapshot</id>
        <name>Gomyck proxy Ali Snapshot</name>
        <url>http://nexus.gomyck.com/nexus/content/repositories/gomyck-repo-snapshot/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```
#### 2.在pom文件中加入依赖:
```xml
<dependency>
    <groupId>com.gomyck</groupId>
    <artifactId>gomyck-fastdfs-spring-boot-starter</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```
#### 3.编辑yml文件(以下为全量配置):
```text
#fastdfs客户端配置
fdfs:
  connect-timeout: 1601
  thumb-image:
    width: 150
    height: 150
  pool:
    jmx-name-prefix: 1
    jmx-name-base: 1
    max-wait-millis: 102
  tracker-list: 
    - 192.168.1.1:22122 #fastdfs服务地址
  so-timeout: 1501
pool:
  max-total: 153

gomyck:
  config:
    redis: true #是否使用redis存储文件上传信息以及上传锁
  redis:
    host: 127.0.0.1 
    password: xxxxx
    port: 6379
  fastdfs: #fastdfs上传配置
    chunk-size: 5 #分块大小, 上传文件分块的大小
    group-id: group1 #fastdfs的组, 文件会被存到这个组下
    file-server-protocol: http #远程文件服务连接协议
    file-server-url: 192.168.1.196 #远程文件服务连接地址
```

#### 4.在静态资源映射表中加入以下配置
```java
@Configuration
public class config extends WebMvcConfigurationSupport {

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("ck-fastdfs/**").addResourceLocations("classpath:/META-INF/resources/ck-fastdfs/");
        //以下两行配置代码为示例页面ckFastDFS.html依赖的一些资源, 在生产环境可以删掉
        registry.addResourceHandler("ck-util/**").addResourceLocations("classpath:/META-INF/resources/ck-util/");
        registry.addResourceHandler("ck-3pty/**").addResourceLocations("classpath:/META-INF/resources/ck-3pty/");
        super.addResourceHandlers(registry);
    }
}
```
#### 5.启动服务, 访问示例页面: host{:port}/{contextPath/}ck-fastdfs/view/ckFastDFS.html (可以渲染表示环境配置成功)

### 二.开发文档

本项目后端服务不在文档说明范围内, 高玩可以自行阅读修改, 只针对前端JS的使用做说明注释

#### 1.在需要开发文件上传的页面(你的业务页面), 引入js: 

> host{:port}/{contextPath/}ck-fastdfs/js/ckFastDFS.js

#### 2.开发文档

##### 1.实例化前端上传实例: 
```javascript
const option = {
    //config something.....
};
const cfd = new CkFastDFS(option);
```
每个实例可以绑定多个上传按钮, 支持id选择器, 类选择器等jq插件支持的选择器类型

多实例存在的场景: 多个文件服务分组, 当不同的按钮上传文件到不同分组时, 可能需要页面多实例来处理

##### 2.option参数说明: 
参数名 | 参数说明 | 是否必填 
---|---
baseURI | 后端服务的URI(包括上下文) | 否
1 | row 2 col 2 | 阿萨德