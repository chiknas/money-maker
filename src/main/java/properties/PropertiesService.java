package properties;

import com.google.gson.Gson;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public class PropertiesService {

    private final Gson gson;

    @Inject
    public PropertiesService(Gson gson) {
        this.gson = gson;
    }

    /**
     * Loads the current properties setup for the specified properties class.
     */
    public <T> Optional<T> loadProperties(Class<T> propertiesClass) {
        Optional<String> suffix = Optional.ofNullable(propertiesClass.getAnnotation(PropertySuffix.class)).
                map(suffixAnnotation -> suffixAnnotation.value() + ".");
        return Optional.ofNullable(PropertiesService.class.getClassLoader().getResourceAsStream("application.properties")).map(resourceAsStream -> {
            Properties appProps = new Properties();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] map = line.split("=");
                    boolean isValuePresent = map.length > 1;
                    if (isValuePresent) {
                        String propertyCode = suffix.map(suffixCode -> map[0].replace(suffixCode, "")).orElse(map[0]);
                        appProps.put(propertyCode, map[1]);
                    }

                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return gson.fromJson(gson.toJson(appProps), propertiesClass);
        });

    }
}
