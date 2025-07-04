package nathan314.ace_race;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SaveCheckpointCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        double z = Double.parseDouble(args[2]);
        AceraceSettings.addCheckpoint(new Location(player.getWorld(), x, y, z));

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        Location player_location = player.getTargetBlockExact(5).getLocation();
        double[] coords = { player_location.getX(), player_location.getY(), player_location.getZ() };

        if (args.length >= 1 && args.length <= 3) {
            double value = coords[args.length - 1];
            return List.of(String.format("%.0f", value), "~");
        }

        return Collections.emptyList();
    }
}
