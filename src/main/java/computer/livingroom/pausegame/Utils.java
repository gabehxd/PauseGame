package computer.livingroom.pausegame;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class Utils {
    public static void runTaskWithPossibleDelay(Runnable runnable, int possibleDelay) {
        if (possibleDelay > 0)
            Bukkit.getScheduler().runTaskLater(PauseGame.getInstance(), runnable, possibleDelay);
        else
            runnable.run();
    }
}
