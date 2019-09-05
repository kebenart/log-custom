log-custom使用

引入包

<dependency>
    <groupId>cn.kebena</groupId>
    <artifactId>log-custom</artifactId>
    <version>1.0-RELEASE</version>
</dependency>
忽略Spring本身的logging

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
        <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
导入log4j2.xml

 <!--定义日志储存文件目录-->
<properties>
    <property name="LOG_HOME">${sys:user.home}/logs/项目名</property>
</properties>
<Appenders>
    <!--控制台输出所有日志-->
    <Console name="Console" target="SYSTEM_OUT">
        <PatternLayout pattern="%date{yyyy-MM-dd HH:mm:ss} %-5level [%t] %F-%M-%L  %msg%n"/>
    </Console>
    <!--全部日志输出-->
    <RollingRandomAccessFile name="ConsoleFile"
                             fileName="${LOG_HOME}/${date:yyyyMMdd}/console.log"
                             filePattern="${LOG_HOME}/${date:yyyyMMdd}/console-%d{yyyyMMdd}-%i.log">
        <PatternLayout pattern="%date{yyyy-MM-dd HH:mm:ss} %-5level [%t] %F-%M-%L  %msg%n"/>
        <Policies>
            <TimeBasedTriggeringPolicy/>
            <SizeBasedTriggeringPolicy size="10MB"/>
        </Policies>
    </RollingRandomAccessFile>
    <!--Info级别日志输出-->
    <RollingRandomAccessFile name="InfoFile"
                             fileName="${LOG_HOME}/${date:yyyyMMdd}/info.log"
                             filePattern="${LOG_HOME}/${date:yyyyMMdd}/info-%d{yyyyMMdd}-%i.log">
        <Filters>
            <ThresholdFilter level="warn" onMatch="DENY" onMismatch="NEUTRAL"/>
            <ThresholdFilter level="info" onMatch="ACCEPT" onMismatch="DENY"/>
        </Filters>
        <PatternLayout pattern="%date{yyyy-MM-dd HH:mm:ss} %-5level [%t] %F-%M-%L  %msg%n"/>
        <Policies>
            <TimeBasedTriggeringPolicy/>
            <SizeBasedTriggeringPolicy size="10MB"/>
        </Policies>
    </RollingRandomAccessFile>
    <!--warn级别日志输出-->
    <RollingRandomAccessFile name="WarnFile"
                             fileName="${LOG_HOME}/${date:yyyyMMdd}/warn.log"
                             filePattern="${LOG_HOME}/${date:yyyyMMdd}/warn-%d{yyyyMMdd}-%i.log">
        <Filters>
            <ThresholdFilter level="error" onMatch="DENY" onMismatch="NEUTRAL"/>
            <ThresholdFilter level="warn" onMatch="ACCEPT" onMismatch="DENY"/>
        </Filters>
        <PatternLayout pattern="%date{yyyy-MM-dd HH:mm:ss} %-5level [%t] %F-%M-%L  %msg%n"/>
        <Policies>
            <TimeBasedTriggeringPolicy/>
            <SizeBasedTriggeringPolicy size="10MB"/>
        </Policies>
    </RollingRandomAccessFile>

    <!--Error级别日志输出-->
    <RollingRandomAccessFile name="ErrorFile"
                             fileName="${LOG_HOME}/${date:yyyyMMdd}/error.log"
                             filePattern="${LOG_HOME}/${date:yyyyMMdd}/error-%d{yyyyMMdd}-%i.log">
        <Filters>
            <ThresholdFilter level="error" onMatch="ACCEPT" onMismatch="DENY"/>
        </Filters>
        <PatternLayout pattern="%date{yyyy-MM-dd HH:mm:ss} %-5level [%t] %F-%M-%L  %msg%n"/>
        <Policies>
            <TimeBasedTriggeringPolicy/>
            <SizeBasedTriggeringPolicy size="10MB"/>
        </Policies>

    </RollingRandomAccessFile>

    <YunlspEmail name="Mail" subject="邮件标题" to="收件人邮箱" from="发件人邮箱"
            smtpHost="smtp.mxhichina.com"  smtpPort="25" smtpPassword="发件人邮箱密码" smtpUsername="发件人邮箱"
            bufferSize="30">

        <HTMLLayout charset="UTF-8" title="邮件标题" locationInfo="true"/>

        <Filters>
            <ThresholdFilter level="error" onMatch="ACCEPT" onMismatch="DENY"/>
        </Filters>

        <!-- 最先执行的策略放在最上面 -->
        <Strategies>
            <StmpStrategy interval="60" entry="1"/>
            <StmpStrategy interval="120" entry="10"/>
            <StmpStrategy interval="180" entry="1"/>
        </Strategies>
    </YunlspEmail>

</Appenders>

<!--然后定义logger，只有定义了logger并引入的appender，appender才会生效-->
<Loggers>
    <AsyncLogger name="com.keben.miniappbook.resolver" level="info"  includeLocation="true" additivity="false">
        <AppenderRef ref="Console"/>
        <AppenderRef ref="InfoFile"/>
        <AppenderRef ref="WarnFile"/>
        <AppenderRef ref="ErrorFile"/>
        <AppenderRef ref="ConsoleFile"/>
        <AppenderRef ref="Mail"/>
    </AsyncLogger>
    <Root level="info">
        <AppenderRef ref="Console"/>
    </Root>
</Loggers>
引入config

在application.yml里配置

logging:
  config: classpath:config/log4j2.xml





