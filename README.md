## experimental-class-define-support-v2312-test

- For https://github.com/apache/shardingsphere/pull/30138 .

- Execute the following command on the Ubuntu 22.04.3 instance with `SDKMAN!` installed.
  Make sure you have the Docker engine installed to start the testcontainers-java container.
- Please ensure that the local port `26403` is available. This is a limitation on the seata client side.
  There is currently no way to dynamically set `service.default.grouplist` in `file.conf`.

```shell
sdk install java 21.0.2-graalce
sdk use java 21.0.2-graalce
sudo apt-get install build-essential libz-dev zlib1g-dev -y

git clone git@github.com:linghengqian/experimental-class-define-support-v2312-test.git
cd ./experimental-class-define-support-v2312-test/

./mvnw -e -T1C clean test

./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy

./mvnw -PnativeTestInSeata -T1C -e clean test
```

- `./mvnw -e -T1C clean test` ensures that the unit test is executed normally,
  and the **errors** are thrown deliberately to ensure normal rollback of the database.
- `./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy` will
  carry GraalVM Tracing Agent and `experimental-class-define-support=true` with `buildArg` to execute unit tests
  and create GraalVM Reachability Metadata JSON file.
- `./mvnw -PnativeTestInSeata -T1C -e clean test` will execute nativeTest.
- `./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy` will fail, the error log is as follows.

```shell
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 13.96 s -- in com.lingh.SeataTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- native:0.10.0:merge-agent-files (test-native) @ experimental-class-define-support-v2312-test ---
[INFO] Merging agent 1 files into /home/linghengqian/TwinklingLiftWorks/git/public/experimental-class-define-support-v2312-test/target/native/agent-output/test
[INFO] 
[INFO] --- native:0.10.0:test (test-native) @ experimental-class-define-support-v2312-test ---
[INFO] Skipping native-image tests (parameter 'skipTests' or 'skipNativeTests' is true).
[INFO] 
[INFO] --- native:0.10.0:metadata-copy (default-cli) @ experimental-class-define-support-v2312-test ---
[WARNING] Destination directory /home/linghengqian/TwinklingLiftWorks/git/public/experimental-class-define-support-v2312-test/src/test/resources/META-INF/native-image/io.seata/seata-all/2.0.0/ doesn't exist.
[WARNING] Creating directory at: /home/linghengqian/TwinklingLiftWorks/git/public/experimental-class-define-support-v2312-test/src/test/resources/META-INF/native-image/io.seata/seata-all/2.0.0/
[INFO] Found GraalVM installation from JAVA_HOME variable.
[INFO] Copying files from: test
[ERROR] Metadata copy process failed with code: 1
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  18.953 s (Wall Clock)
[INFO] Finished at: 2024-02-19T00:08:43+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.graalvm.buildtools:native-maven-plugin:0.10.0:metadata-copy (default-cli) on project experimental-class-define-support-v2312-test: Metadata copy process failed. -> [Help 1]
org.apache.maven.lifecycle.LifecycleExecutionException: Failed to execute goal org.graalvm.buildtools:native-maven-plugin:0.10.0:metadata-copy (default-cli) on project experimental-class-define-support-v2312-test: Metadata copy process failed.
    at org.apache.maven.lifecycle.internal.MojoExecutor.doExecute2 (MojoExecutor.java:333)
    at org.apache.maven.lifecycle.internal.MojoExecutor.doExecute (MojoExecutor.java:316)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:212)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:174)
    at org.apache.maven.lifecycle.internal.MojoExecutor.access$000 (MojoExecutor.java:75)
    at org.apache.maven.lifecycle.internal.MojoExecutor$1.run (MojoExecutor.java:162)
    at org.apache.maven.plugin.DefaultMojosExecutionStrategy.execute (DefaultMojosExecutionStrategy.java:39)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:159)
    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:105)
    at org.apache.maven.lifecycle.internal.builder.multithreaded.MultiThreadedBuilder$1.call (MultiThreadedBuilder.java:193)
    at org.apache.maven.lifecycle.internal.builder.multithreaded.MultiThreadedBuilder$1.call (MultiThreadedBuilder.java:180)
    at java.util.concurrent.FutureTask.run (FutureTask.java:317)
    at java.util.concurrent.Executors$RunnableAdapter.call (Executors.java:572)
    at java.util.concurrent.FutureTask.run (FutureTask.java:317)
    at java.util.concurrent.ThreadPoolExecutor.runWorker (ThreadPoolExecutor.java:1144)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run (ThreadPoolExecutor.java:642)
    at java.lang.Thread.run (Thread.java:1583)
Caused by: org.apache.maven.plugin.MojoExecutionException: Metadata copy process failed.
    at org.graalvm.buildtools.maven.MetadataCopyMojo.executeCopy (MetadataCopyMojo.java:163)
    at org.graalvm.buildtools.maven.MetadataCopyMojo.execute (MetadataCopyMojo.java:115)
    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:126)
    at org.apache.maven.lifecycle.internal.MojoExecutor.doExecute2 (MojoExecutor.java:328)
    at org.apache.maven.lifecycle.internal.MojoExecutor.doExecute (MojoExecutor.java:316)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:212)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:174)
    at org.apache.maven.lifecycle.internal.MojoExecutor.access$000 (MojoExecutor.java:75)
    at org.apache.maven.lifecycle.internal.MojoExecutor$1.run (MojoExecutor.java:162)
    at org.apache.maven.plugin.DefaultMojosExecutionStrategy.execute (DefaultMojosExecutionStrategy.java:39)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:159)
    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:105)
    at org.apache.maven.lifecycle.internal.builder.multithreaded.MultiThreadedBuilder$1.call (MultiThreadedBuilder.java:193)
    at org.apache.maven.lifecycle.internal.builder.multithreaded.MultiThreadedBuilder$1.call (MultiThreadedBuilder.java:180)
    at java.util.concurrent.FutureTask.run (FutureTask.java:317)
    at java.util.concurrent.Executors$RunnableAdapter.call (Executors.java:572)
    at java.util.concurrent.FutureTask.run (FutureTask.java:317)
    at java.util.concurrent.ThreadPoolExecutor.runWorker (ThreadPoolExecutor.java:1144)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run (ThreadPoolExecutor.java:642)
    at java.lang.Thread.run (Thread.java:1583)
[ERROR] 
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException

```