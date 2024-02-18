- experimental-class-define-support-v2312-test

- For https://github.com/apache/shardingsphere/pull/30138 .

- Execute the following command on the Ubuntu 22.04.3 instance with `SDKMAN!` installed.
  Make sure you have the Docker engine installed to start the testcontainers-java container.
- Please ensure that the local port `26403` is available. This is a limitation on the seata client side.
  There is currently no way to dynamically set `service.default.grouplist`.

```shell
sdk install java 21.0.2-graalce
sdk use java 21.0.2-graalce
sudo apt-get install build-essential libz-dev zlib1g-dev -y

git clone git@github.com:linghengqian/experimental-class-define-support-v2312-test.git
cd ./experimental-class-define-support-v2312-test/
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy
```

- Log.