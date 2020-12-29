package main;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.FileUtils;
import commands.CreateGameInstanceCommand;
import commands.JoinGameInstanceCommand;
import commands.StartGameInstanceCommand;
import commands.TestCommand;
import listeners.PlayerJoinListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import util.DatabaseManager;
import profiles.PlayerProfiles;

import java.io.File;
import java.sql.Connection;

public class GameManagerPlugin extends JavaPlugin {

    //Connection vars
    static Connection connection; //This is the variable we will use to connect to database

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        FileConfiguration config = getConfig();

        DatabaseManager.connect(
                "localhost",
                config.getString("mysql.database"),
                config.getString("mysql.username"),
                config.getString("mysql.password"));

        DatabaseManager.executeUpdate("create_profiles_table.sql");

        PlayerProfiles.loadPlayerProfiles();

        //register commands
        getCommand("test").setExecutor(new TestCommand());
        getCommand("creategame").setExecutor(new CreateGameInstanceCommand());
        getCommand("joingame").setExecutor(new JoinGameInstanceCommand());
        getCommand("startgame").setExecutor(new StartGameInstanceCommand());

        //register event listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

    }

    @Override
    public void onDisable() {
        DatabaseManager.disconnect();

        MVWorldManager worldManager = JavaPlugin.getPlugin(MultiverseCore.class).getMVWorldManager();
        File root = new File(System.getProperty("user.dir"));

        System.out.println("Root folder: "+root.getAbsolutePath());

        for (MultiverseWorld w: worldManager.getMVWorlds()) {
            String name = w.getName();
            if (name.startsWith("game_")) {
                getLogger().info("Found game instance "+name+" in Multiverse config");
                if (worldManager.removeWorldFromConfig(w.getName())) {
                    getLogger().info("Deleted game "+name+" from Multiverse config");
                }
                File gameInstanceFolder = new File(root.getAbsolutePath()+"/"+name);
                if (gameInstanceFolder.exists()) {
                    if (FileUtils.deleteFolder(gameInstanceFolder)) {
                        getLogger().info("Deleted game folder "+gameInstanceFolder.getAbsolutePath());
                    }
                }
            }
        }

    }

    public static Plugin getInstance() {
        return JavaPlugin.getPlugin(GameManagerPlugin.class);
    }

}
