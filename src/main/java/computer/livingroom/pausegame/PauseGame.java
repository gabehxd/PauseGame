package computer.livingroom.pausegame;

import computer.livingroom.pausegame.Listeners.ModCompanionListener;
import computer.livingroom.pausegame.Listeners.PauseGameListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class PauseGame extends JavaPlugin {
    @Getter
    private static PauseGame instance;
    @Getter
    private final Settings settings = new Settings();
    public static final String PAUSE_KEY = "pausegame:sync";
    public static final String SUPPORTED_KEY = "pausegame:supported";

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PauseGameListener(), this);
        if (settings.enableModSupport()) {
            ModCompanionListener listener = new ModCompanionListener();
            getServer().getPluginManager().registerEvents(listener, this);
            getServer().getMessenger().registerOutgoingPluginChannel(this, SUPPORTED_KEY);
            getServer().getMessenger().registerIncomingPluginChannel(this, PAUSE_KEY, listener);
        }
        getLogger().info("PauseGame Initialized");
    }

    public class Settings {
        public int getDelay() {
            return PauseGame.this.getConfig().getInt("task-delay-in-ticks", 1);
        }

        public boolean shouldRunGC() {
            return PauseGame.this.getConfig().getBoolean("run-gc", false);
        }

        public boolean shouldSaveGame() {
            return PauseGame.this.getConfig().getBoolean("save-game", true);
        }

        public boolean enableModSupport() {
            return PauseGame.this.getConfig().getBoolean("enable-mod-companion", true);
        }

        public boolean freezePauseMenuPlayers()
        {
            return PauseGame.this.getConfig().getBoolean("freeze-pause-menu-players", true);
        }
    }
}
