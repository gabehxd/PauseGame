package computer.livingroom.pausegame;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.World;

import java.util.logging.Logger;

@UtilityClass
public class Utils {

    public static void freezeGameNoStep() {
        freezeGame(false);
    }

    public static void freezeGameWithStep() {
        freezeGame(true);
    }

    private static void freezeGame(boolean step) {
        PauseGame instance = PauseGame.getInstance();
        Logger logger = instance.getLogger();
        PauseGame.Settings settings = instance.getSettings();

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
        logger.info("Pausing game...");
        tickManager.setFrozen(true);
        if (step)
            tickManager.stepGameIfFrozen(settings.getSteps());
    }
}
