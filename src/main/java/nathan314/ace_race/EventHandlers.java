package nathan314.ace_race;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.UUID;

public class EventHandlers implements Listener {
    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Block block_below = world.getBlockAt(player.getLocation().subtract(new Vector(0, 1, 0)));
        Material material_below = block_below.getType();
        switch (material_below) {
            case EMERALD_BLOCK -> {
                Acerace.getInstance().launchGreen(player);
            }
            case REDSTONE_BLOCK -> {
                Acerace.getInstance().launchRed(player);
            }
            case GOLD_BLOCK -> {
                Acerace.getInstance().launchOrangeHigh(player);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        Block block_below = world.getBlockAt(player.getLocation().subtract(new Vector(0, 1, 0)));
        Material material_below = block_below.getType();
        Block block_at = world.getBlockAt(player.getLocation());
        Material material_at = block_at.getType();

        switch (material_below) {
            case IRON_BLOCK -> {
                Acerace.getInstance().launchOrange(player);
            }
        }
        switch (material_at) {
            case TRIPWIRE ->  {
                Tripwire tripwireData = (Tripwire) block_at.getBlockData();
                if (tripwireData.isAttached()) {
                    Acerace.getInstance().lap(player);
                } else {
                    Acerace.getInstance().checkpoint(player, block_at);
                }
            }
        }
        if (player.getFallDistance() > 10 && !(Acerace.getInstance().has_launch_orange_high.get(player.getUniqueId())) && player.getGameMode() != GameMode.CREATIVE) {
            Acerace.getInstance().respawn(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Acerace.getInstance().default_location = new Location(player.getWorld(), 141, 141, 21);
        Acerace.getInstance().has_launch_orange_high.put(uuid, false);
        Acerace.getInstance().player_respawns.put(uuid, new Location(player.getWorld(), 141, 141, 21));
        Acerace.resetPlayer(player);
    }
}
