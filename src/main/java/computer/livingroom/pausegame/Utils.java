package computer.livingroom.pausegame;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.World;

import java.util.function.BiConsumer;
import java.util.logging.Logger;

@UtilityClass
public class Utils {
    private static final BiConsumer<PauseGame.Settings, Logger> consumer = (settings, logger) -> {
            if (settings.shouldSaveGame()) {
                logger.info("Saving game...");
                Bukkit.getServer().savePlayers();
                for (World world : Bukkit.getServer().getWorlds()) {
                    logger.info("Saving chunks for level '" + world.getName() + "'");
                    world.save();
                }
                logger.info("All dimensions are saved");
            }

            ServerTickManager tickManager = Bukkit.getServerTickManager();
            if (!tickManager.isFrozen()) {
                logger.info("Pausing game...");
                tickManager.setFrozen(true);
            }
        };

    public static void runTaskWithPossibleDelay(Runnable runnable, int possibleDelay) {
        if (possibleDelay > 0)
            Bukkit.getScheduler().runTaskLater(PauseGame.getInstance(), runnable, possibleDelay);
        else
            runnable.run();
    }

    public static void instantServerFreeze() {
        PauseGame instance = PauseGame.getInstance();
        Logger logger = instance.getLogger();
        PauseGame.Settings settings = instance.getSettings();

        consumer.accept(settings, logger);
    }

    public static void runFreezeTask() {
        PauseGame instance = PauseGame.getInstance();
        Logger logger = instance.getLogger();
        PauseGame.Settings settings = instance.getSettings();
        //This needs to run on the next tick or soon after this tick so the server can unload chunks
        logger.info("Delaying task by " + settings.getDelay() + " tick(s)");
        Utils.runTaskWithPossibleDelay(() -> consumer.accept(settings, logger), settings.getDelay());
    }
}
