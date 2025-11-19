package at.shorty.polar.addon.util;

import at.shorty.polar.addon.PolarLogs;
import com.cjcrafter.foliascheduler.FoliaCompatibility;
import com.cjcrafter.foliascheduler.ServerImplementation;
import lombok.Getter;

@Getter
public class SpecialUtilityJustForFoliaSpecialNeeds {

    private final ServerImplementation serverImplementation;

    public SpecialUtilityJustForFoliaSpecialNeeds(PolarLogs plugin) {
        serverImplementation = new FoliaCompatibility(plugin).getServerImplementation();
    }

    public void runAsyncNow(Runnable runnable) {
        serverImplementation.async().runNow(runnable);
    }

}
