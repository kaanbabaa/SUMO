package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.sumo.libtraci.TraCIColor;

public class Colors {
    public static final TraCIColor RED     = new TraCIColor(255, 0,   0,   255);
    public static final TraCIColor GREEN   = new TraCIColor(0,   255, 0,   255);
    public static final TraCIColor BLUE    = new TraCIColor(0,   0,   255, 255);
    public static final TraCIColor YELLOW  = new TraCIColor(255, 255, 0,   255);
    public static final TraCIColor CYAN    = new TraCIColor(0,   255, 255, 255);
    public static final TraCIColor MAGENTA = new TraCIColor(255, 0,   255, 255);
    public static final TraCIColor WHITE   = new TraCIColor(255, 255, 255, 255);
    public static final TraCIColor BLACK   = new TraCIColor(0,   0,   0,   255);
    public static final TraCIColor GRAY    = new TraCIColor(128, 128, 128, 255);
    public static final TraCIColor ORANGE  = new TraCIColor(255, 165, 0,   255);
    public static final TraCIColor PURPLE  = new TraCIColor(255, 0, 255, 255);
    
    public static final Map<String, TraCIColor> COLOR_MAP = new LinkedHashMap<>();
    
    static {
        COLOR_MAP.put("Red", RED);
        COLOR_MAP.put("Blue", BLUE);
        COLOR_MAP.put("Green", GREEN);
        COLOR_MAP.put("Yellow", YELLOW);
        COLOR_MAP.put("White", WHITE);
        COLOR_MAP.put("Orange", ORANGE);
        COLOR_MAP.put("Magenta", MAGENTA);
        COLOR_MAP.put("Cyan", CYAN);
    }
    
    private static final Random random = new Random();
    
    public static TraCIColor getRandomColor() {
        List<TraCIColor> values = new ArrayList<>(COLOR_MAP.values());
        return values.get(random.nextInt(values.size()));
    }
    
}