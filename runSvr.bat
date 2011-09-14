java -version

@echo off
SET CP_VAR=./bin;D:/ProgramFiles/apache-mina/dist/mina-core-2.0.3.jar;D:/ProgramFiles/apache-mina/lib/commons-lang-2.6.jar;D:/ProgramFiles/apache-mina/lib/commons-logging-1.0.3.jar;D:/ProgramFiles/apache-mina/lib/javassist-3.11.0.GA.jar;D:/ProgramFiles/apache-mina/lib/javassist-3.7.ga.jar;D:/ProgramFiles/apache-mina/lib/jcl-over-slf4j-1.6.1.jar;D:/ProgramFiles/apache-mina/lib/jzlib-1.0.7.jar;D:/ProgramFiles/apache-mina/lib/ognl-3.0.1.jar;D:/ProgramFiles/apache-mina/lib/slf4j-api-1.6.1.jar;D:/ProgramFiles/apache-mina/lib/slf4j-jdk14-1.6.1.jar;D:/ProgramFiles/apache-mina/lib/spring-2.5.6.jar;D:/ProgramFiles/apache-mina/lib/tomcat-apr-5.5.23.jar;D:/ProgramFiles/apache-mina/lib/xbean-spring-3.7.jar;./lib/mybatis-3.0.5.jar;./lib/mysql-connector-java-5.1.16-bin.jar;

@echo on

java -cp %CP_VAR%      studentpal.engine.ServerEngine


