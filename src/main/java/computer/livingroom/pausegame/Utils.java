package computer.livingroom.pausegame;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ServerTickManager;
import org.bukkit.World;

import java.util.logging.Logger;

@UtilityClass
public class Utils {
    public static void freezeGame(boolean isQuit) {
        PauseGame instance = PauseGame.getInstance();
        Logger logger = instance.getLogger();
        PauseGame.Settings settings = instance.getSettings();

        if (isQuit && settings.shouldSaveGame()) {
            logger.info("Saving game...");
            Bukkit.getServer().savePlayers();
            for (World world : Bukkit.getServer().getWorlds()) {
                logger.info("Saving chunks for level '" + world.getName() + "'");
                world.save();
                for (Chunk loadedChunk : world.getLoadedChunks()) {
                    loadedChunk.unload();
                }
            }
            logger.info("All dimensions are saved");
        }


        ServerTickManager tickManager = Bukkit.getServerTickManager();
        if (!tickManager.isFrozen()) {
            logger.info("Pausing game...");
            tickManager.setFrozen(true);
        }
    }
}
