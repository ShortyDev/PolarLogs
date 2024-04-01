package at.shorty.polar.addon.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class LogCountData {

    public Map<String, Integer> mitigations;
    public Map<String, Integer> detections;
    public Map<String, Integer> cloudDetections;
    public Map<String, Integer> punishments;

    public int getTotalCount() {
        return mitigations.values().stream().mapToInt(Integer::intValue).sum() +
                detections.values().stream().mapToInt(Integer::intValue).sum() +
                cloudDetections.values().stream().mapToInt(Integer::intValue).sum() +
                punishments.values().stream().mapToInt(Integer::intValue).sum();
    }
}
