package com.example.canteen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "canteen")
public class CanteenProperties {

    private Order order = new Order();
    private Scheduler scheduler = new Scheduler();
    private Image image = new Image();

    @Data
    public static class Order {
        private String start;
        private String end;
        private String zone;
    }

    @Data
    public static class Scheduler {
        private String dailyResetCron;
    }

    @Data
    public static class Image {
        private String uploadDir;
        private long maxBytes;
        private int minPixel;
        private int maxPixel;
    }
}