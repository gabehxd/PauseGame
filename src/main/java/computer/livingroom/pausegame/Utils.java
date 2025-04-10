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
            Bukkit.getServer().savePlayers();
            for (World world : Bukkit.getServer().getWorlds()) {
                logger.info("Saving chunks for level '" + world.getName() + "'");
                world.save();
                for (Chunk loadedChunk : world.getLoadedChunks()) {
                    loadedChunk.unload(false);
                }
            }
            logger.info("All dimensions are saved");
        }

        //Paper actually has a delay of ~10 second on chunk unloads
        Runnable runnable = () -> {
            ServerTickManager tickManager = Bukkit.getServerTickManager();
            if (!tickManager.isFrozen()) {
                logger.info("Pausing game...");
                tickManager.setFrozen(true);
            }
        };

        if (settings.getDelay() <= 0 || !isQuit)
            runnable.run();
        else
            //+1 tick since the player will finish leaving on the next tick when this is called
            Bukkit.getScheduler().runTaskLater(PauseGame.getInstance(), runnable, timeToTicks(settings.getDelay()) + 1);

    }

    //ty @ShaneBeee for this

    /**
     * Check if server is running a minimum Minecraft version
     *
     * @param major    Major version to check (Most likely just going to be 1)
     * @param minor    Minor version to check
     * @param revision Revision to check
     * @return True if running this version or higher
     */
    public static boolean isRunningMinecraft(int major, int minor, int revision) {
        String[] version = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");
        int maj = Integer.parseInt(version[0]);
        int min = Integer.parseInt(version[1]);
        int rev;
        try {
            rev = Integer.parseInt(version[2]);
        } catch (Exception ignore) {
            rev = 0;
        }
        return maj > major || min > minor || (min == minor && rev >= revision);
    }

    /**
     * @param seconds Seconds
     * @return Time of seconds in ticks.
     */
    public static int timeToTicks(int seconds) {
        return seconds * 20;
    }
}
