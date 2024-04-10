package at.shorty.polar.addon.data;

import lombok.Data;

@Data
public class LogEntry {

    public String type;
    public String playerName;
    public String playerUuid;
    public String playerVersion;
    public int playerLatency;
    public String playerBrand;
    public double vl;
    public String checkType;
    public String checkName;
    public String details;
    public String punishmentType;
    public String punishmentReason;
    public long time;

    public LogEntry(String type, String playerName, String playerUuid, String playerVersion, int playerLatency, String playerBrand, double vl, String checkType, String checkName, String details, String punishmentType, String punishmentReason, long time) {
        this.type = type;
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.playerVersion = playerVersion;
        this.playerLatency = playerLatency;
        this.playerBrand = playerBrand;
        this.vl = Math.round(vl);
        this.checkType = checkType;
        this.checkName = checkName;
        this.details = details;
        this.punishmentType = punishmentType;
        this.punishmentReason = punishmentReason;
        this.time = time;
        if (this.type == null) this.type = "null";
        if (this.playerName == null) this.playerName = "null";
        if (this.playerUuid == null) this.playerUuid = "null";
        if (this.playerVersion == null) this.playerVersion = "null";
        if (this.playerBrand == null) this.playerBrand = "null";
        if (this.checkType == null) this.checkType = "null";
        if (this.checkName == null) this.checkName = "null";
        if (this.details == null) this.details = "null";
        if (this.punishmentType == null) this.punishmentType = "null";
        if (this.punishmentReason == null) this.punishmentReason = "null";
    }

}
