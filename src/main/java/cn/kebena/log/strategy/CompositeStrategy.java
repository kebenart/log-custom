package cn.kebena.log.strategy;

import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author Keben
 * @description 发送邮件策略 多个
 * @date 2019-09-05 15:23
 */


@Plugin(name = "Strategies", category = "Core", printObject = true)
public final class CompositeStrategy extends AbstractStrategy {

    private static final Strategy[] EMPTY_STRATEGIES = new Strategy[0];
    private final Strategy[] strategies;

    private CompositeStrategy() {
        this.strategies = EMPTY_STRATEGIES;
    }

    private CompositeStrategy(final Strategy[] strategies) {
        this.strategies = strategies == null ? EMPTY_STRATEGIES : strategies;
    }

    public CompositeStrategy addStrategy(final Strategy strategy) {
        if (strategy == null) {
            // null does nothing
            return this;
        }
        if (strategy instanceof CompositeStrategy) {
            final int size = this.strategies.length + ((CompositeStrategy) strategy).size();
            final Strategy[] copy = Arrays.copyOf(this.strategies, size);
            final int index = this.strategies.length;
            for (final Strategy currentStrategy : ((CompositeStrategy) strategy).strategies) {
                copy[index] = currentStrategy;
            }
            return new CompositeStrategy(copy);
        }
        final Strategy[] copy = Arrays.copyOf(this.strategies, this.strategies.length + 1);
        copy[this.strategies.length] = strategy;
        return new CompositeStrategy(copy);
    }

    public CompositeStrategy removeStrategy(final Strategy strategy) {
        if (strategy == null) {
            // null does nothing
            return this;
        }
        // This is not a great implementation but simpler than copying Apache Commons
        // Lang ArrayUtils.removeElement() and associated bits (MutableInt),
        // which is OK since removing a strategy should not be on the critical path.
        final List<Strategy> strategyList = new ArrayList<>(Arrays.asList(this.strategies));
        if (strategy instanceof CompositeStrategy) {
            for (final Strategy currentStrategy : ((CompositeStrategy) strategy).strategies) {
                strategyList.remove(currentStrategy);
            }
        } else {
            strategyList.remove(strategy);
        }
        return new CompositeStrategy(strategyList.toArray(new Strategy[this.strategies.length - 1]));
    }

    public Strategy[] getStrategies() {
        return strategies;
    }

    /**
     * Returns whether this composite contains any strategies.
     *
     * @return whether this composite contains any strategies.
     */
    public boolean isEmpty() {
        return this.strategies.length == 0;
    }

    @Override
    public Long getInterval() {
        return 0L;
    }

    @Override
    public Integer getEntry() {
        return 0;
    }

    public int size() {
        return strategies.length;
    }

    @Override
    public void start() {
        this.setStarting();
        for (final Strategy strategy : strategies) {
            strategy.start();
        }
        this.setStarted();
    }

    @Override
    public boolean stop(final long timeout, final TimeUnit timeUnit) {
        this.setStopping();
        for (final Strategy strategy : strategies) {
            if (strategy instanceof LifeCycle2) {
                ((LifeCycle2) strategy).stop(timeout, timeUnit);
            } else {
                strategy.stop();
            }
        }
        setStopped();
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strategies.length; i++) {
            if (sb.length() == 0) {
                sb.append('{');
            } else {
                sb.append(", ");
            }
            sb.append(strategies[i].toString());
        }
        if (sb.length() > 0) {
            sb.append('}');
        }
        return sb.toString();
    }


    /**
     * Creates a CompositeStrategy.
     *
     * @param strategies An array of Strategies to call.
     * @return The CompositeStrategy.
     */
    @PluginFactory
    public static CompositeStrategy createStrategies(@PluginElement("Strategies") final Strategy[] strategies) {
        return new CompositeStrategy(strategies);
    }

}
