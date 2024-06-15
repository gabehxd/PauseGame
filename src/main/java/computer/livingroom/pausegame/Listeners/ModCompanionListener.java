package computer.livingroom.pausegame.Listeners;

import computer.livingroom.pausegame.PauseGame;
import computer.livingroom.pausegame.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ModCompanionListener implements Listener, PluginMessageListener {
    private final ArrayList<Player> pausedPlayers = new ArrayList<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(PauseGame.getInstance(), () -> {
            PauseGame.getInstance().getLogger().info("Sending support packet");
            event.getPlayer().sendPluginMessage(PauseGame.getInstance(), PauseGame.SUPPORTED_KEY, new byte[]{});
        }, 5);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        if (!event.getPlayer().isOp())
            event.getPlayer().setAllowFlight(false);
        pausedPlayers.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (!Bukkit.getServer().getServerTickManager().isFrozen())
            return;

        if (pausedPlayers.contains(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!Bukkit.getServer().getServerTickManager().isFrozen())
            return;

        if (pausedPlayers.contains(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!Bukkit.getServer().getServerTickManager().isFrozen())
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityAirChangeEvent(EntityAirChangeEvent event) {
        if (!Bukkit.getServer().getServerTickManager().isFrozen())
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (!Bukkit.getServer().getServerTickManager().isFrozen())
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (!Bukkit.getServer().getServerTickManager().isFrozen())
            return;

        event.setCancelled(true);
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
                    Utils.freezeGameNoStep();
                    for (Player pausedPlayer : pausedPlayers) {
                        if (!pausedPlayer.isOp())
                            pausedPlayer.setAllowFlight(true);
                    }
                }
            } else {
                for (Player pausedPlayer : pausedPlayers) {
                    if (!pausedPlayer.isOp())
                        pausedPlayer.setAllowFlight(false);
                }
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
