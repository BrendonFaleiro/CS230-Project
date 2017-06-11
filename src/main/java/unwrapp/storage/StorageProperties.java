package unwrapp.storage;

/**
 * Created by Shubham Mittal on 6/07/17.
 */


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {
    private String location = "src/main/resources/original";
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

}
