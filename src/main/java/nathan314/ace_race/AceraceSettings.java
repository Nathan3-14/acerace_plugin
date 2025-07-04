package nathan314.ace_race;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class AceraceSettings {
    private final static AceraceSettings instance = new AceraceSettings();

    private File file;
    private YamlConfiguration config;
//    private static HashMap<String, List<HashMap<String, Object>>> checkpoints;
    private static HashMap<String, List<HashMap<String, Object>>> checkpoints;
    private static String current_map;
    private static List<String> maps = new ArrayList<>();


    public void load() {
        file = new File(Acerace.getInstance().getDataFolder(), "config.yml");

        if (!file.exists()) {
            Acerace.getInstance().saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        config.options().parseComments(true);

        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        current_map = config.getString("current_map");
        checkpoints = new HashMap<>();
        MemorySection checkpoints_temp = (MemorySection) config.get("checkpoints");
        for (String key: checkpoints_temp.getKeys(false)) {
            List<Map<? ,?>> t = config.getMapList("checkpoints." + key);
            List<HashMap<String, Object>> a = new ArrayList<>();
            for (Map<?, ?> map: t) {
//                if (areAllKeysStrings(map) && areAllValuesObjects(map)) {
                    a.add((HashMap<String, Object>) map);
                    checkpoints.put(key, a);
//                }
            }
            maps.add(key);
        }
    }
    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void set(String path, Object value) {
        instance.config.set(path, value);
        instance.save();
    }

    public static void addCheckpoint(Location checkpoint_location) {
        Map<String, Object> new_checkpoint = new HashMap<>();

        new_checkpoint.put("world", checkpoint_location.getWorld().getName());
        new_checkpoint.put("x", checkpoint_location.getX());
        new_checkpoint.put("y", checkpoint_location.getY());
        new_checkpoint.put("z", checkpoint_location.getZ());

        List<Map<?, ?>> current_checkpoints = instance.config.getMapList("checkpoints."+current_map);
        current_checkpoints.add(new_checkpoint);
        set("checkpoints."+current_map, current_checkpoints);
    }

    public static Location loadCheckpoint(HashMap<String, Object> checkpoint) {
        World world = Bukkit.getWorld((String) checkpoint.get("world"));
        Double x = (Double) checkpoint.get("x");
        Double y = (Double) checkpoint.get("y");
        Double z = (Double) checkpoint.get("z");
        return new Location(world, x, y, z);
    }

    public static List<Location> getCheckpoints() {
        List<Location> checkpoint_locations = new ArrayList<>();
        for (HashMap<String, Object> checkpoint: checkpoints.get(current_map)) {
            checkpoint_locations.add(loadCheckpoint(checkpoint));
        }
        return checkpoint_locations;
    }

    public static void changeMap(String new_map) {
        set("current_map", new_map);
        current_map = new_map;
    }

    public static List<String> getMaps() {
        return maps;
    }

    public static AceraceSettings getInstance() {
        return instance;
    }
}
