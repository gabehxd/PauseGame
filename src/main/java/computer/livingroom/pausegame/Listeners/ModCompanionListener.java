package computer.livingroom.pausegame.Listeners;

import computer.livingroom.pausegame.PauseGame;
import computer.livingroom.pausegame.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ModCompanionListener implements Listener, PluginMessageListener {
    private final ArrayList<Player> pausedPlayers = new ArrayList<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(PauseGame.getInstance(), () -> {
            PauseGame.getInstance().getLogger().info("Sending support packet.");
            event.getPlayer().sendPluginMessage(PauseGame.getInstance(), PauseGame.SUPPORTED_KEY, new byte[]{});
        }, 20);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (!PauseGame.getInstance().getSettings().freezePauseMenuPlayers())
            return;

        if (pausedPlayers.contains(event.getPlayer()) && Bukkit.getServer().getServerTickManager().isFrozen()) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(PauseGame.PAUSE_KEY))
            return;

        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(message));
        try {
            boolean paused = stream.readBoolean();

            if (paused) {
                pausedPlayers.add(player);

                if (pausedPlayers.size() == Bukkit.getOnlinePlayers().size()) {
                    Utils.RunFreezeTask();
                }
            } else {
                pausedPlayers.remove(player);

                ServerTickManager tickManager = Bukkit.getServerTickManager();
                if (tickManager.isFrozen()) {
                    PauseGame.getInstance().getLogger().info("Unpausing game...");
                    tickManager.setFrozen(false);
                }
            }

        } catch (IOException e) {
            PauseGame.getInstance().getLogger().warning("Player " + player.getName() + " sent malformed data! ");
        }
    }
}
