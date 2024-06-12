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
        if (settings.enableModSupport()) {
            ModCompanionListener listener = new ModCompanionListener();
            getServer().getPluginManager().registerEvents(listener, this);
            getServer().getMessenger().registerOutgoingPluginChannel(this, SUPPORTED_KEY);
            getServer().getMessenger().registerIncomingPluginChannel(this, PAUSE_KEY, listener);
        }
        getServer().getPluginManager().registerEvents(new PauseGameListener(), this);
        getLogger().info("PauseGame Initialized");
    }

    public class Settings {
        public int getSteps() {
            int ticks = PauseGame.this.getConfig().getInt("task-delay-in-ticks", -1);
            if (ticks < 0) {
                ticks = PauseGame.this.getConfig().getInt("step-ticks", 1);
                if (ticks < 0) {
                    return 0;
                }
            }
            return ticks;
        }

        public boolean shouldSaveGame() {
            return PauseGame.this.getConfig().getBoolean("save-game", false);
        }

        public boolean enableModSupport() {
            return PauseGame.this.getConfig().getBoolean("enable-mod-companion", true);
        }

        public boolean freezePauseMenuPlayers() {
            return PauseGame.this.getConfig().getBoolean("freeze-pause-menu-players", true);
        }
    }
}
