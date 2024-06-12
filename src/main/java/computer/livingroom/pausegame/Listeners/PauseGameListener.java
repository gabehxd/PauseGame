package computer.livingroom.pausegame.Listeners;

import computer.livingroom.pausegame.PauseGame;
import computer.livingroom.pausegame.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;

public class PauseGameListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerStart(ServerLoadEvent event) {
        if (event.getType().equals(ServerLoadEvent.LoadType.STARTUP)) {
            //We don't really need to save or anything as the server JUST started
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

        PauseGame.getInstance().getLogger().info("Running freeze task due to a player leaving");
        Utils.RunFreezeTask();
    }
}

