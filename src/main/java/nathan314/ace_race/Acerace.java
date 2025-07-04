package nathan314.ace_race;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;


public final class Acerace extends JavaPlugin implements Listener {
    public Location default_location;
    public static Acerace instance;

    HashMap<UUID, Location> player_respawns = new HashMap<>();
    HashMap<UUID, Boolean> has_launch_orange_high = new HashMap<>();
    public HashMap<UUID, LinkedHashMap<Location, Long>> player_checkpoint_times = new HashMap<>();
    public HashMap<UUID, LinkedHashMap<Location, Long>> player_old_section_times = new HashMap<>();
    public HashMap<UUID, Location> player_last_checkpoint_visited = new HashMap<>();
    public HashMap<UUID, Long> player_last_lap_times = new HashMap<>();


    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EventHandlers(), this);
        getCommand("savecheckpoint").setExecutor(new SaveCheckpointCommand());
        getCommand("savecheckpoint").setTabCompleter(new SaveCheckpointCommand());
        getCommand("resetcheckpoints").setExecutor(new ResetCheckpointsCommand());
        getCommand("resetcheckpoints").setTabCompleter(new ResetCheckpointsCommand());
        getCommand("goto").setExecutor(new GoToCommand());
        getCommand("changemap").setExecutor(new ChangeMapCommand());
        getCommand("changemap").setTabCompleter(new ChangeMapCommand());
        instance = this;
        default_location.set(154.5, 132.0, 41.5);
        AceraceSettings.getInstance().load();
    }

    public static Acerace getInstance() {
        return instance;
    }

    public static void resetPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        Acerace.getInstance().player_checkpoint_times.put(uuid, new LinkedHashMap<Location, Long>());
        Acerace.getInstance().player_old_section_times.put(uuid, new LinkedHashMap<Location, Long>());
        for (Location checkpoint: AceraceSettings.getCheckpoints()) {
            Acerace.getInstance().player_checkpoint_times.get(uuid).put(checkpoint, System.currentTimeMillis());
            Acerace.getInstance().player_old_section_times.get(uuid).put(checkpoint, 99999L);
        }
    }

    public void launchRed(Player player) {
        Vector player_dir = player.getLocation().getDirection();
        Vector new_vel = new Vector(0, 0 , 0);
        new_vel.setX(player_dir.getX());
        new_vel.setY(0.75);
        new_vel.setZ(player_dir.getZ());
        player.setVelocity(new_vel.multiply(1.15));
        has_launch_orange_high.put(player.getUniqueId(), false);
    }

    public void launchGreen(Player player) {
        Vector new_vel = player.getVelocity();
        new_vel.setY(1.25);
        player.setVelocity(new_vel);
        has_launch_orange_high.put(player.getUniqueId(), false);
    }

    public void launchOrange(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        has_launch_orange_high.put(player.getUniqueId(), false);
    }

    public void launchOrangeHigh(Player player) {
        Vector new_vel = player.getLocation().getDirection();
        new_vel.multiply(0.5);
        new_vel.setY(2);
        player.setVelocity(new_vel);
        has_launch_orange_high.put(player.getUniqueId(), true);
    }

    public void checkpoint_respawn(Player player, Block block_at) {
        Location new_location = block_at.getLocation();
        new_location.setDirection(player.getLocation().getDirection());
        player_respawns.put(player.getUniqueId(), new_location);
    }

    private String ms_to_s(Long time_ms) {
        long time_s = time_ms / 1000;
        return (time_s) + "." + (time_ms-(time_s*1000));
    }
    public void checkpoint(Player player, Block block_at) {
        checkpoint_respawn(player, block_at);

        //* Timing System *//
        Long now = System.currentTimeMillis();
        LinkedHashMap<Location, Long> current_times = player_checkpoint_times.get(player.getUniqueId());
        LinkedHashMap<Location, Long> current_old_section_times = player_old_section_times.get(player.getUniqueId());

        for (Location checkpoint_location: AceraceSettings.getCheckpoints()) {
            if (checkpoint_location.distance(player.getLocation()) < 4) {
                // Check if triggering the same checkpoint twice
                if (!(player_last_checkpoint_visited.get(player.getUniqueId()) == null)) {
                    if (player_last_checkpoint_visited.get(player.getUniqueId()).equals(checkpoint_location)) {
                        return;
                    }
                }


                int current_checkpoint_index = LinkedHashmapFunctions.indexOfKey(current_times, checkpoint_location);
                int previous_checkpoint_index = current_checkpoint_index - 1;
                if (previous_checkpoint_index < 0) previous_checkpoint_index = current_times.size() - 1;

                long section_time_ms = now - LinkedHashmapFunctions.getValueAtIndex(current_times, previous_checkpoint_index);
                long section_time_old_ms = player_old_section_times.get(player.getUniqueId()).get(checkpoint_location);

                TextComponent message_section_time_difference;
                if (section_time_ms < section_time_old_ms) {
                    message_section_time_difference = Component.text("-" + ms_to_s(section_time_old_ms - section_time_ms) + "s", NamedTextColor.GREEN, TextDecoration.BOLD);
                } else if (section_time_ms > section_time_old_ms) {
                    message_section_time_difference = Component.text("+" + ms_to_s(section_time_ms - section_time_old_ms )+ "s", NamedTextColor.RED, TextDecoration.BOLD);
                } else {
                    message_section_time_difference = Component.text(" 0.000s", NamedTextColor.GRAY, TextDecoration.BOLD);
                }

                TextComponent message_section_time = Component
                        .text("Section time: " + ms_to_s(section_time_ms) + "s ", NamedTextColor.GRAY, TextDecoration.BOLD)
                        .append(message_section_time_difference)
                        ;
                player.sendActionBar(message_section_time);
                // /title @s actionbar {text: "-1.520s", color: "green", bold: 1}
                // /title @s actionbar {text: "+2.841s", color: "red", bold: 1}

                // Register Checkpoint and Previous
                current_times.put(checkpoint_location, now);
                current_old_section_times.put(checkpoint_location, section_time_ms);
                player_last_checkpoint_visited.put(player.getUniqueId(), checkpoint_location);
            }
        }
    }

    public void lap(Player player) {
        long now = System.currentTimeMillis();

        if (player_last_lap_times.containsKey(player.getUniqueId())) {
            long old_time = player_last_lap_times.get(player.getUniqueId());
            long elapsed = now - old_time;
            if (elapsed < 1000) { // ignore close ones + respawns
                return;
            }
            Component message = Component.text("Lap time " + elapsed + "ms", NamedTextColor.GOLD, TextDecoration.BOLD);
            player.sendTitlePart(TitlePart.TITLE, Component.text(""));
            player.sendTitlePart(TitlePart.SUBTITLE, message);
        } else {
            Component message = Component.text("Starting new Lap", NamedTextColor.GOLD, TextDecoration.BOLD);
            player.sendTitlePart(TitlePart.TITLE, Component.text(""));
            player.sendTitlePart(TitlePart.SUBTITLE, message);
        }

        player_last_lap_times.put(player.getUniqueId(), now);
        checkpoint(player, player.getWorld().getBlockAt(player.getLocation()));
    }

    public void respawn(Player player) {
        player.teleport(player_respawns.get(player.getUniqueId()));
        has_launch_orange_high.put(player.getUniqueId(), false);
        player.setFallDistance(0);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
