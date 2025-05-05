package at.shorty.polar.addon;

import at.shorty.polar.addon.config.Logs;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HikariPooledConnection {

    private final HikariConfig hikariConfig;
    private HikariDataSource handle;

    public void initializePool() {
        handle = new HikariDataSource(hikariConfig);
    }

    public HikariDataSource getPool() {
        return handle;
    }

    public void close() {
        if (handle != null && !handle.isClosed()) {
            handle.close();
        }
    }

    public static HikariConfig prepareConfig(Logs.Database credentials, int maxPoolSize) {
        String driver = "com.mysql.cj.jdbc.Driver";
        try {
            loadDriver("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            PolarLogs.getPlugin(PolarLogs.class).getLogger().warning("Couldn't load com.mysql.cj.jdbc.Driver, falling back to com.mysql.jdbc.Driver");
            try {
                loadDriver("com.mysql.jdbc.Driver");
                driver = "com.mysql.jdbc.Driver";
            } catch (ClassNotFoundException ex) {
                PolarLogs.getPlugin(PolarLogs.class).getLogger().severe("Failed to load MySQL driver. Logging disabled.");
                return null;
            }
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl("jdbc:mysql://" + credentials.sqlHost + ":" + credentials.sqlPort + "/" + credentials.sqlDatabase + "?autoReconnect=true&useSSL=" + credentials.useSsl);
        config.setUsername(credentials.sqlUsername);
        config.setPassword(credentials.sqlPassword);
        config.setConnectionTestQuery("SELECT 1");
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(1);
        return config;
    }

    private static void loadDriver(String className) throws ClassNotFoundException {
        Class.forName(className);
    }

}
