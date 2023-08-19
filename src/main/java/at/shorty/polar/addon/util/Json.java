package at.shorty.polar.addon.util;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Json {

    public String render(Object object) {
        return new Gson().toJson(object);
    }

}
