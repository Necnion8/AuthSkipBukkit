package com.gmail.necnionch.myplugin.authskip.bukkit;

import com.gmail.necnionch.myplugin.authskip.bukkit.util.MySessionService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class AuthSkipPlugin extends JavaPlugin {
    private MySessionService mySessionService;

    @Override
    public void onEnable() {
        if (!Bukkit.getOnlineMode()) {
            getLogger().warning("The server is in offline mode.");
            setEnabled(false);
            return;
        }

        registerSessionService();
    }

    @Override
    public void onDisable() {
        unregisterSessionService();
    }


    public void registerSessionService() {
        try {
            mySessionService = MySessionService.create(Bukkit.getServer());
            if (mySessionService.register()) {
                getLogger().info("Registered custom session service");
            } else {
                getLogger().warning("Failed to register custom session service");
            }
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Failed to register custom session service", e);
        }
    }

    public void unregisterSessionService() {
        if (mySessionService != null) {
            try {
                if (mySessionService.unregister()) {
                    getLogger().info("Unregistered custom session service");
                } else {
                    getLogger().warning("Failed to unregister custom session service");
                }
            } catch (Throwable e) {
                getLogger().warning("Failed to unregister custom session service");
            }
        }
        mySessionService = null;
    }
}
