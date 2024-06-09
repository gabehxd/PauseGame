package computer.livingroom.pausegame;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class PauseGame extends JavaPlugin {
    @Getter
    private static PauseGame instance;
    @Getter
    private final Settings settings = new Settings();

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PauseGameListener(), this);
        getLogger().info("PauseGame Initialized");
    }

    class Settings {
        public int getDelay()
        {
            return PauseGame.this.getConfig().getInt("task-delay-in-ticks", 1);
        }

        public boolean shouldRunGC()
        {
            return PauseGame.this.getConfig().getBoolean("run-gc", false);
        }

        public boolean shouldSaveGame()
        {
            return PauseGame.this.getConfig().getBoolean("save-game", true);
        }

        public boolean forceUnloadChunks()
        {
            return PauseGame.this.getConfig().getBoolean("force-unload-chunks", false);
        }
    }
}
