package computer.livingroom.pausegame.Listeners;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import computer.livingroom.pausegame.PauseGame;
import computer.livingroom.pausegame.Utils;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EssentialsListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void OnPlayerAFKEvent(AfkStatusChangeEvent event) {
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        boolean isAllAfk = true;
        for (User onlineUser : essentials.getOnlineUsers()) {
            if (!onlineUser.isAfk()) {
                isAllAfk = false;
                break;
            }
        }

        if (isAllAfk) {
            Utils.RunFreezeTask();
        } else {
            ServerTickManager tickManager = Bukkit.getServerTickManager();
            if (tickManager.isFrozen()) {
                PauseGame.getInstance().getLogger().info("Unpausing game...");
                tickManager.setFrozen(false);
            }
        }
    }
}
