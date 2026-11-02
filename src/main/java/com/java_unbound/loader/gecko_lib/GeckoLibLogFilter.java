package com.java_unbound.loader.gecko_lib;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class GeckoLibLogFilter {
    private static boolean Initialized = false;

    public static void Initialize() {
        if (Initialized) {return;}

        LoggerContext Context = (LoggerContext) LogManager.getContext(false);
        Configuration Configuration = Context.getConfiguration();
        LoggerConfig Logger = Configuration.getLoggerConfig("GeckoLib");

        Logger.addFilter(new AbstractFilter() {
            @Override
            public Result filter(LogEvent Event) {
                if (!"GeckoLib".equals(Event.getLoggerName())) {
                    return Result.NEUTRAL;
                }

                if (Event.getLevel() != Level.ERROR) {
                    return Result.NEUTRAL;
                }

                String Message = Event.getMessage().getFormattedMessage();

                if (Message.startsWith("Unable to find model:")) {
                    return Result.DENY;
                }

                return Result.NEUTRAL;
            }
        });

        Context.updateLoggers();

        Initialized = true;
    }
}