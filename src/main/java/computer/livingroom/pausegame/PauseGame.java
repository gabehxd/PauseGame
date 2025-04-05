package computer.livingroom.pausegame;

import computer.livingroom.pausegame.Listeners.PauseGameListener;
import lombok.Getter;
import org.bukkit.Bukkit;
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
        //Features we are using are only supported since 1.20.4!
        if (!Utils.isRunningMinecraft(1, 20, 4)) {
            this.getLogger().severe("Only Minecraft versions 1.20.4 and above are supported!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PauseGameListener(), this);
        getLogger().info("PauseGame Initialized");
    }

    public class Settings {
        public boolean shouldSaveGame() {
            return PauseGame.this.getConfig().getBoolean("save-game-on-quit", true);
        }
    }
}
