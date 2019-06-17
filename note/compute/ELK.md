## Elasticsearch, Logstash 和 Kibana


使用 logstash 读取 mysql 数据写入 elasticsearch 中，命令
```
logstash -f config\p1.conf --config.reload.automatic
```
p1.conf 内容
```conf
input {
  jdbc {
    jdbc_driver_library => "path\mysql-connector-java-8.0.13.jar"
    jdbc_driver_class => "com.mysql.cj.jdbc.Driver"
    jdbc_connection_string => "jdbc:mysql://localhost:3306/taptap?useSSL=true&characterEncoding=utf8&useLegacyDatetimeCode=false&serverTimezone=UTC&allowMultiQueries=true"
    jdbc_user => "root"
    jdbc_password => "xx"
    schedule => "* * * * *" 
    statement => "SELECT * from tap_post_info"
  }
}

output {
  elasticsearch {
    hosts => ["http://localhost:9200"]
    #index => "%{[@metadata][beat]}-%{[@metadata][version]}-%{+YYYY.MM.dd}"
	index => "tap_posts"
  }
}
```

#### Elasticsearch java 快速链接
[Java Low Level REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-low.html)

[Java High Level REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)

[Transport Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html)

#### Elasticsearch python 快速链接
[elasticsearch-py](https://www.elastic.co/guide/en/elasticsearch/client/python-api/current/index.html)

filebeat.yml 日志文件，启动命令
```
filebeat -e -c filebeat.yml -d "publish"
```
```yml
filebeat.inputs:

- type: log

  # Change to true to enable this input configuration.
  enabled: true

  # Paths that should be crawled and fetched. Glob based paths.
  paths:
    - C:\Program Files\Filebeat\logstash-tutorial-dataset
    #- c:\programdata\elasticsearch\logs\*

output.logstash:
   #The Logstash hosts
  hosts: ["localhost:5044"]
  #output.elasticsearch:
  # Array of hosts to connect to.
  #hosts: ["localhost:9200"]
```

