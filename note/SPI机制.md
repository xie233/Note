
开发过程中可能存在一套接口，对应不同的厂商的实现，比如数据库的驱动，还有日志插件等，需要 SPI(Service Provider Interface) 机制，来实现有效地解耦。

### 示例

* spi
    * interfaces 制定的接口
    * exam 测试
    * imp1 实现1
    * imp2 实现2

以上是多模块结构，简单的 spi 机制实现。

#### interfaces 
提供接口
```java
package com.xie.api;
public interface Connector {
    void connect();
}
```

#### imp1
实现 1，在 pom.xml 导入包 interfaces，
```java
package com.xie.api;
public class Connector1  implements Connector {
    @Override
    public void connect() {
        System.out.println("connecting Connector1");
    }
}
```
且在 resources 目录下创建文件 META-INF/services/com.xie.api.Connector, 指定加载类
```
com.xie.api.Connector1
```

#### imp2
实现 2，在 pom.xml 导入包 interfaces，
```java
package com.xie.api;
public class Connector2  implements Connector {
    @Override
    public void connect() {
        System.out.println("connecting Connector2");
    }
}
```
且在 resources 目录下创建文件 META-INF/services/com.xie.api.Connector, 指定加载类
```
com.xie.api.Connector2
```

#### exam
测试 spi 机制代码，

```java
package demo;
import com.xie.api.Connector;
import java.util.ServiceLoader;
public class APP {
    public static void main(String[] args) {
        ServiceLoader<Connector> loader = ServiceLoader.load(Connector.class);
        for (Connector connector: loader){
            connector.connect();
        }
    }
}
```
对于具体的连接器，只需在 pom.xml 里面设置，下面加载 imp1 的实现
```maven
       <dependency>
            <groupId>com.xie</groupId>
            <artifactId>interfaces</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>com.xie</groupId>
            <artifactId>imp1</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

查看 ServiceLoader 类，默认是在 META-INF/services/ 目录下搜索文件，
```
private static final String PREFIX = "META-INF/services/";
```