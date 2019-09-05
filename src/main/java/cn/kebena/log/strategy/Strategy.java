package cn.kebena.log.strategy;

import org.apache.logging.log4j.core.LifeCycle;

/**
 * @author Keben
 * @description 发送邮件策略 检测一段时间内 的条目数
 * 借鉴于Redis的RDB快照
 * @date 2019-09-05 11:23
 */
public interface Strategy extends LifeCycle {

    /**
     * 检测一段时间内
     *
     * @return
     */
    Long getInterval();

    /**
     * 条目数
     *
     * @returnE
     */
    Integer getEntry();
}
