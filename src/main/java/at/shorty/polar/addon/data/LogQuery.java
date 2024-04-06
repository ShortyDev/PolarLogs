package at.shorty.polar.addon.data;

import at.shorty.polar.addon.util.TimeRange;
import lombok.extern.log4j.Log4j2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class LogQuery {

    public String player;
    public String context;
    public TimeRange timeRange;
    public QueryType type;

    public static LogQuery parseFrom(String query) {
        Pattern playerPattern = Pattern.compile("^p:([a-zA-Z0-9_]{3,16})([@:][a-zA-Z0-9_)]+)?([@:][a-zA-Z0-9_)]+)?$");
        Pattern contextPattern = Pattern.compile("^c:([a-zA-Z0-9_]{3,16})(:[a-zA-Z0-9_)]+)?$");
        Matcher playerMatcher = playerPattern.matcher(query);
        Matcher contextMatcher = contextPattern.matcher(query);
        LogQuery logQuery = new LogQuery();
        if (playerMatcher.matches()) {
            logQuery.player = playerMatcher.group(1);
            logQuery.type = QueryType.PLAYER;
            for (int i = 2; i < playerMatcher.groupCount() + 1; i++) {
                if (playerMatcher.group(i) != null) {
                    String part = playerMatcher.group(i);
                    switch (part.charAt(0)) {
                        case '@':
                            logQuery.context = part.substring(1);
                            break;
                        case ':':
                            logQuery.timeRange = TimeRange.parseFromString(part.substring(1));
                            break;
                        default:
                            break;
                    }
                }
            }
        } else if (contextMatcher.matches()) {
            logQuery.context = contextMatcher.group(1);
            logQuery.type = QueryType.CONTEXT;
            for (int i = 2; i < contextMatcher.groupCount() + 1; i++) {
                if (contextMatcher.group(i) != null) {
                    String part = contextMatcher.group(i);
                    if (part.charAt(0) == ':') {
                        logQuery.timeRange = TimeRange.parseFromString(part.substring(1));
                    }
                }
            }
        }
        return logQuery;
    }

    public enum QueryType {
        PLAYER,
        CONTEXT
    }

}
