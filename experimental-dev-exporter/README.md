### Please follow below instructions to run the application: 
#### [Assuming that you already have Splunk enterprise instance ready to use]

1. Steps to configure HEC. Refer this [link](https://docs.splunk.com/Documentation/Splunk/latest/Data/UsetheHTTPEventCollector#Configure_HTTP_Event_Collector_on_Splunk_Enterprise) for configuration on Splunk Enterprise. 
   1. While configuration, please uncheck HTTPS settings. We have tested this app for http protocol only for now.
2. Extract app_extension.zip file which has two folders:
   1. Extension 
   2. Java-app-example
3. Navigate to extension folder, perform below steps:
   1. Once you have your HEC token ready (refer #1), replace that token to the location below.
     /extension/src/main/java/com/example/javaagent/CustomHECLogExporter.java [Line 32]
   2. From /extension directory, run command: **./gradlew build**
   3. After successful build, run command: **chmod 777 ./build/libs/***
4. Navigate to java-app-example folder, run command: **mvn package**
5. Launch new terminal window, run below command: <br/>
  **java
  -javaagent:{FULL_PATH_OF_extension_folder}/build/libs/opentelemetry-javaagent.jar -jar {FULL_PATH_OF_app-example_folder}/target/java-app-example.jar**
