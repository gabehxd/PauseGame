package computer.livingroom.pausegame;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ServerTickManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.logging.Logger;

public class EssentialsListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void OnPlayerAFKEvent(AfkStatusChangeEvent event) {
        if (event.getValue()) {
            Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            boolean isAllAfk = true;
            for (User onlineUser : essentials.getOnlineUsers()) {
                if (!onlineUser.isAfk()) {
                    isAllAfk = false;
                    break;
                }
            }

            PauseGame instance = PauseGame.getInstance();
            Logger logger = instance.getLogger();
            PauseGame.Settings settings = instance.getSettings();

            if (isAllAfk) {
                logger.info("All players are AFK...");
                if (settings.shouldSaveGame()) {
                    logger.info("Saving game...");
                    Bukkit.getServer().savePlayers();
                    for (World world : Bukkit.getServer().getWorlds()) {
                        logger.info("Saving chunks for level '" + world.getName() + "'");
                        world.save();
                        if (settings.forceUnloadChunks()) {
                            int cnt = 0;
                            for (Chunk chunk : world.getLoadedChunks()) {
                                if (!chunk.isForceLoaded()) {
                                    if (chunk.unload(false))
                                        cnt++;
                                }
                            }
                            logger.info("Unloaded " + cnt + " chunks in " + world.getName());
                        }
                    }
                    logger.info("All dimensions are saved");
                }
                //Double-check the player count is empty
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    ServerTickManager tickManager = Bukkit.getServerTickManager();
                    if (!tickManager.isFrozen()) {
                        logger.info("Pausing game...");
                        tickManager.setFrozen(true);
                    }
                }

                if (settings.shouldRunGC()) {
                    logger.info("Calling GC...");
                    System.gc();
                }
            }
        }
    }
}
