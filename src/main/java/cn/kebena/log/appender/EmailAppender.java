/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package cn.kebena.log.appender;

import cn.kebena.log.strategy.StmpStrategy;
import cn.kebena.log.strategy.Strategy;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.ValidPort;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.util.Booleans;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Keben
 * @description 自定义邮件源
 * @date 2019-09-05 15:23
 */
@Plugin(name = "YunlspEmail", category = Core.CATEGORY_NAME, elementType = "appender", printObject = true)
public final class EmailAppender extends AbstractAppender {

    private static final int DEFAULT_BUFFER_SIZE = 512;

    private ScheduledExecutorService taskService;

    /**
     * The SMTP Manager
     */
    private final EmailManager manager;

    private EmailAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
                          final EmailManager manager, final boolean ignoreExceptions, final Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.manager = manager;

        taskService = Executors.newScheduledThreadPool(1);
        LOGGER.info("邮件定时任务已初始化...");
        Runnable run = () -> {
            Boolean isSend = manager.strategy();
            if (isSend) {
                manager.sendEvents(getLayout(), manager.pop());
            }
        };

        taskService.scheduleWithFixedDelay(run, 10, 60, TimeUnit.SECONDS);
    }

    /**
     * Create a SmtpAppender.
     *
     * @param name         The name of the Appender.
     * @param to           The comma-separated list of recipient email addresses.
     * @param cc           The comma-separated list of CC email addresses.
     * @param bcc          The comma-separated list of BCC email addresses.
     * @param from         The email address of the sender.
     * @param replyTo      The comma-separated list of reply-to email addresses.
     * @param subject      The subject of the email message.
     * @param smtpProtocol The SMTP transport protocol (such as "smtps", defaults to "smtp").
     * @param smtpHost     The SMTP hostname to send to.
     * @param smtpPortStr  The SMTP port to send to.
     * @param smtpUsername The username required to authenticate against the SMTP server.
     * @param smtpPassword The password required to authenticate against the SMTP server.
     * @param smtpDebug    Enable mail session debuging on STDOUT.
     *                     message?
     * @param layout       The layout to use (defaults to HtmlLayout).
     * @param filter       The Filter or null (defaults to ThresholdFilter, level of
     *                     ERROR).
     * @param ignore       If {@code "true"} (default) exceptions encountered when appending events are logged; otherwise
     *                     they are propagated to the caller.
     * @return The SmtpAppender.
     */
    @PluginFactory
    public static EmailAppender createAppender(
            @PluginConfiguration final Configuration config,
            @PluginAttribute("name") @Required final String name,
            @PluginAttribute("to") final String to,
            @PluginAttribute("cc") final String cc,
            @PluginAttribute("bcc") final String bcc,
            @PluginAttribute("from") final String from,
            @PluginAttribute("replyTo") final String replyTo,
            @PluginAttribute("subject") final String subject,
            @PluginAttribute("smtpProtocol") final String smtpProtocol,
            @PluginAttribute("smtpHost") final String smtpHost,
            @PluginAttribute(value = "smtpPort", defaultString = "0") @ValidPort final String smtpPortStr,
            @PluginAttribute("smtpUsername") final String smtpUsername,
            @PluginAttribute(value = "smtpPassword", sensitive = true) final String smtpPassword,
            @PluginAttribute("smtpDebug") final String smtpDebug,
            @PluginAttribute("bufferSize") final String bufferSizeStr,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Strategy") Strategy strategy,
            @PluginAttribute("ignoreExceptions") final String ignore) {
        if (name == null) {
            LOGGER.error("No name provided for SmtpAppender");
            return null;
        }

        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        final int smtpPort = AbstractAppender.parseInt(smtpPortStr, 0);
        final boolean isSmtpDebug = Boolean.parseBoolean(smtpDebug);

        final int bufferSize = bufferSizeStr == null ? DEFAULT_BUFFER_SIZE : Integer.parseInt(bufferSizeStr);
        if (layout == null) {
            layout = HtmlLayout.createDefaultLayout();
        }
        if (filter == null) {
            filter = ThresholdFilter.createFilter(null, null, null);
        }
        // 默认
        if (strategy == null) {
            strategy = StmpStrategy.createStrategy("60", "1");
        }

        final Configuration configuration = config != null ? config : new DefaultConfiguration();


        final EmailManager manager = EmailManager.getSmtpManager(configuration, to, cc, bcc, from, replyTo, subject, smtpProtocol,
                smtpHost, smtpPort, smtpUsername, smtpPassword, isSmtpDebug, filter.toString(), bufferSize, strategy);

        return new EmailAppender(name, filter, layout, manager, ignoreExceptions, null);
    }

    /**
     * Capture all events in CyclicBuffer.
     *
     * @param event The Log event.
     * @return true 返回false 就会触发邮件发送
     */
    @Override
    public boolean isFiltered(final LogEvent event) {
        final boolean filtered = super.isFiltered(event);
        if (!filtered) {
            manager.add(event);
        }

        return filtered;
    }

    /**
     * Perform SmtpAppender specific appending actions, mainly adding the event
     * to a cyclic buffer and checking if the event triggers an e-mail to be
     * sent.
     *
     * @param event The Log event.
     */
    @Override
    public void append(final LogEvent event) {
        // error级别  自行把控
    }
}
