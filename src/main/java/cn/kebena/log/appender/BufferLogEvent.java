package cn.kebena.log.appender;

import org.apache.logging.log4j.core.LogEvent;

/**
 * @author Keben
 * @description
 * @date 2019-09-05 13:43
 */
public class BufferLogEvent {
    private LogEvent logEvent;
    private Long createTime;

    public BufferLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
        this.createTime = System.currentTimeMillis();
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }

    public void setLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
