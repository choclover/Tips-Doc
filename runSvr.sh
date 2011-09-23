
java -version

CP_VAR=./bin:./build/classes:/media/Tools/apache-mina-2.0.3/dist/mina-core-2.0.3.jar:/media/Tools/apache-mina-2.0.3/lib/commons-lang-2.6.jar:/media/Tools/apache-mina-2.0.3/lib/commons-logging-1.0.3.jar:/media/Tools/apache-mina-2.0.3/lib/javassist-3.11.0.GA.jar:/media/Tools/apache-mina-2.0.3/lib/javassist-3.7.ga.jar:/media/Tools/apache-mina-2.0.3/lib/jcl-over-slf4j-1.6.1.jar:/media/Tools/apache-mina-2.0.3/lib/jzlib-1.0.7.jar:/media/Tools/apache-mina-2.0.3/lib/ognl-3.0.1.jar:/media/Tools/apache-mina-2.0.3/lib/slf4j-jdk14-1.6.1.jar:/media/Tools/apache-mina-2.0.3/lib/slf4j-api-1.6.1.jar:/media/Tools/apache-mina-2.0.3/lib/spring-2.5.6.jar:/media/Tools/apache-mina-2.0.3/lib/tomcat-apr-5.5.23.jar:/media/Tools/apache-mina-2.0.3/lib/xbean-spring-3.7.jar:./lib/mybatis-3.0.5.jar:./lib/mysql-connector-java-5.1.16-bin.jar:

echo "Classpath is:"
echo $CP_VAR

java -cp $CP_VAR      studentpal.engine.ServerEngine


