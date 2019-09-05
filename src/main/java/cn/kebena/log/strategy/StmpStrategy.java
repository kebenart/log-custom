package cn.kebena.log.strategy;

import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.util.PerformanceSensitive;

/**
 * @author Keben
 * @description 发送邮件策略
 * @date 2019-09-05 15:23
 */
@Plugin(name = "StmpStrategy", category = Node.CATEGORY, printObject = true)
@PerformanceSensitive("allocation")
public class StmpStrategy extends AbstractStrategy {

    private Long interval;

    private Integer entry;

    private StmpStrategy(Long interval, Integer entry) {
        this.interval = interval;
        this.entry = entry;
    }

    @Override
    public Long getInterval() {
        return interval;
    }

    @Override
    public Integer getEntry() {
        return entry;
    }

    @PluginFactory
    public static StmpStrategy createStrategy(
            @PluginAttribute("interval") final String intervalStr,
            @PluginAttribute("entry") final String entryStr) {
        if (null == intervalStr || null == entryStr) {
            throw new NullPointerException("[StmpStrategy] interval or size is null");
        }
        long interval = Long.parseLong(intervalStr);
        int entry = Integer.parseInt(entryStr);
        return new StmpStrategy(interval, entry);
    }


    @Override
    public String toString() {
        return "[ interval:" + interval + ", entry:" + entry + "]";
    }
}
