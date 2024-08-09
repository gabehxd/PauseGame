package computer.livingroom.pausegame.Listeners;

import com.google.common.collect.ImmutableList;
import computer.livingroom.pausegame.PauseGame;
import computer.livingroom.pausegame.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import org.bukkit.util.Vector;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ModCompanionListener implements Listener, PluginMessageListener {
    private final HashSet<Player> pausedPlayers = new HashSet<>(1);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(PauseGame.getInstance(), () -> {
            PauseGame.getInstance().getLogger().info("Sending support packet");
            event.getPlayer().sendPluginMessage(PauseGame.getInstance(), PauseGame.SUPPORTED_KEY, new byte[]{});
        }, 100);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL))
            event.getPlayer().setAllowFlight(false);

        pausedPlayers.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (!Bukkit.getServer().getServerTickManager().isFrozen())
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!Bukkit.getServer().getServerTickManager().isFrozen())
            return;

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

    private final HashMap<Player, Vector> velocity = new HashMap<>(1);

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(PauseGame.PAUSE_KEY))
            return;

        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(message));
        try {
            boolean paused = stream.readBoolean();

            PauseGame.getInstance().getLogger().fine(player.getName() + " sent sync packet with: " + paused);
            PauseGame.getInstance().getLogger().fine("Paused players: "+ pausedPlayers.size());
            PauseGame.getInstance().getLogger().fine("Online players: "+ Bukkit.getOnlinePlayers().size()+ "\n");
            if (paused) {
                pausedPlayers.add(player);
                PauseGame.getInstance().getLogger().fine("Added player to paused list");
                PauseGame.getInstance().getLogger().fine("Paused players: "+ pausedPlayers.size());
                PauseGame.getInstance().getLogger().fine("Online players: "+ Bukkit.getOnlinePlayers().size() + "\n");
                if (pausedPlayers.size() == Bukkit.getOnlinePlayers().size() && !Bukkit.getServer().getServerTickManager().isFrozen()) {
                    for (Player pausedPlayer : pausedPlayers) {
                        if (pausedPlayer.getGameMode().equals(GameMode.SURVIVAL))
                            pausedPlayer.setAllowFlight(true);

                        velocity.put(pausedPlayer, pausedPlayer.getVelocity());
                    }
                    Utils.freezeGame(false);
                }
            } else {
                ImmutableList<Player> list =  ImmutableList.copyOf(pausedPlayers);
                pausedPlayers.remove(player);
                PauseGame.getInstance().getLogger().fine("Removed player from paused list");
                PauseGame.getInstance().getLogger().fine("Paused players: "+ pausedPlayers.size());
                PauseGame.getInstance().getLogger().fine("Online players: "+ Bukkit.getOnlinePlayers().size() + "\n");
                if (Bukkit.getServer().getServerTickManager().isFrozen()) {
                    //I am paranoid
                    for (Player pausedPlayer : list) {
                        if (pausedPlayer.getGameMode().equals(GameMode.SURVIVAL))
                            pausedPlayer.setAllowFlight(false);
                    }

                    ServerTickManager tickManager = Bukkit.getServerTickManager();
                    if (tickManager.isFrozen()) {
                        PauseGame.getInstance().getLogger().info("Unpausing game...");
                        tickManager.setFrozen(false);
                    }
                    //I am VeRY paranoid
                    for (Player pausedPlayer : list) {
                        Vector vector = velocity.get(pausedPlayer);
                        if (vector != null)
                            pausedPlayer.setVelocity(vector);
                    }
                    velocity.clear();
                }
            }
        } catch (IOException e) {
            PauseGame.getInstance().getLogger().warning("Player " + player.getName() + " sent malformed data!");
        }
    }
}
