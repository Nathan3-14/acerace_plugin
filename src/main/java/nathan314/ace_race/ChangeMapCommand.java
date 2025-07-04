package nathan314.ace_race;

import org.bukkit.command.*;

import java.util.List;

public class ChangeMapCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        AceraceSettings.changeMap(args[0]);
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return AceraceSettings.getMaps();
        }
        return null;
    }
}
