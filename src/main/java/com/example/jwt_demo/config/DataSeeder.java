package com.example.jwt_demo.config;

import com.example.jwt_demo.model.Landmark;
import com.example.jwt_demo.model.SuggestedPlan;
import com.example.jwt_demo.repository.LandmarkRepository;
import com.example.jwt_demo.repository.SuggestedPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner seedHomeContent(LandmarkRepository landmarks,
                                             SuggestedPlanRepository plans) {
        return args -> {
            if (landmarks.count() == 0) {
                log.info("Seeding landmarks collection...");
                landmarks.saveAll(List.of(
                        landmark("Pyramids of Giza", "The last surviving ancient wonder", "https://images.unsplash.com/photo-1539650116574-75c0c6d73f6e", 29.9792, 31.1342),
                        landmark("Luxor Temple", "Heart of ancient Thebes", "https://images.unsplash.com/photo-1572252009286-268acec5ca0a", 25.6995, 32.6391),
                        landmark("Abu Simbel", "Ramses II's rock-cut masterpiece", "https://images.unsplash.com/photo-1568322445389-f64ac2515020", 22.3372, 31.6258),
                        landmark("Khan el-Khalili", "Cairo's centuries-old bazaar", "https://images.unsplash.com/photo-1571331044905-bbc3a648c2dc", 30.0477, 31.2622),
                        landmark("Siwa Oasis", "Desert springs and salt lakes", "https://images.unsplash.com/photo-1539635278303-d4002c07eae3", 29.2032, 25.5197)
                ));
            }
            if (plans.count() == 0) {
                log.info("Seeding suggested_plans collection...");
                plans.saveAll(List.of(
                        plan("Nile Cruise Adventure", "5 days from Aswan to Luxor", "https://images.unsplash.com/photo-1593696954577-ab3d39317b97",
                                samplePlan("Nile Cruise Adventure", List.of(
                                        sampleDay(180, 8820, List.of(
                                                sampleActivity("08:00 AM", "Aswan High Dam", "Engineering marvel with panoramic Nile views", 10, 490, 23.9707, 32.8770, "attraction"),
                                                sampleActivity("01:00 PM", "Philae Temple", "Island temple dedicated to Isis", 15, 735, 24.0254, 32.8842, "attraction")
                                        ))
                                ))),
                        plan("Cairo Weekend Escape", "2 days exploring the capital", "https://images.unsplash.com/photo-1572252009286-268acec5ca0a",
                                samplePlan("Cairo Weekend Escape", List.of(
                                        sampleDay(95, 4655, List.of(
                                                sampleActivity("09:00 AM", "Pyramids of Giza", "Ancient wonder of the world", 20, 980, 29.9792, 31.1342, "attraction"),
                                                sampleActivity("02:00 PM", "Khan el-Khalili", "Historic bazaar with crafts and food", 25, 1225, 30.0477, 31.2622, "shopping")
                                        ))
                                ))),
                        plan("Red Sea Diving Trip", "4 days in Hurghada", "https://images.unsplash.com/photo-1582967788606-a171c1080cb0",
                                samplePlan("Red Sea Diving Trip", List.of(
                                        sampleDay(220, 10780, List.of(
                                                sampleActivity("07:00 AM", "Giftun Island Snorkeling", "Coral reefs and tropical fish", 60, 2940, 27.1869, 33.9305, "experience")
                                        ))
                                )))
                ));
            }
        };
    }

    private static Landmark landmark(String name, String subtitle, String imageUrl, double lat, double lng) {
        Landmark l = new Landmark();
        l.setName(name);
        l.setSubtitle(subtitle);
        l.setImageUrl(imageUrl);
        l.setLat(lat);
        l.setLng(lng);
        return l;
    }

    private static SuggestedPlan plan(String name, String subtitle, String imageUrl, java.util.Map<String, Object> data) {
        SuggestedPlan p = new SuggestedPlan();
        p.setName(name);
        p.setSubtitle(subtitle);
        p.setImageUrl(imageUrl);
        p.setData(data);
        return p;
    }

    private static java.util.Map<String, Object> samplePlan(String title, List<java.util.Map<String, Object>> days) {
        return java.util.Map.of("title", title, "days", days);
    }

    private static java.util.Map<String, Object> sampleDay(double totalUsd, double totalEgp, List<java.util.Map<String, Object>> activities) {
        return java.util.Map.of(
                "total_cost_usd", totalUsd,
                "total_cost_egp", totalEgp,
                "activities", activities
        );
    }

    private static java.util.Map<String, Object> sampleActivity(String time, String title, String description,
                                                                double usd, double egp, double lat, double lng, String category) {
        java.util.Map<String, Object> a = new java.util.HashMap<>();
        a.put("time", time);
        a.put("title", title);
        a.put("description", description);
        a.put("cost_usd", usd);
        a.put("cost_egp", egp);
        a.put("lat", lat);
        a.put("lng", lng);
        a.put("category", category);
        return a;
    }
}
