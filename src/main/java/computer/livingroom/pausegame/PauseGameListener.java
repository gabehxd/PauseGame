package computer.livingroom.pausegame;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ServerTickManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.logging.Logger;

public class PauseGameListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerStart(ServerLoadEvent event) {
        if (event.getType().equals(ServerLoadEvent.LoadType.STARTUP)) {
            PauseGame.getInstance().getLogger().info("Pausing game...");
            Bukkit.getServerTickManager().setFrozen(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        ServerTickManager tickManager = Bukkit.getServerTickManager();
        if (tickManager.isFrozen()) {
            PauseGame.getInstance().getLogger().info("Unpausing game...");
            tickManager.setFrozen(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        //The server does not update the player count until the next tick.
        if (Bukkit.getOnlinePlayers().size() != 1)
            return;

        PauseGame instance = PauseGame.getInstance();
        Logger logger = instance.getLogger();
        PauseGame.Settings settings = instance.getSettings();

        //This needs to run on the next tick or soon after this tick so the server can unload chunks
        logger.info("Delaying task by " + settings.getDelay() + " tick(s)");
        Utils.runTaskWithPossibleDelay(() -> {
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
        }, settings.getDelay());
    }
}

