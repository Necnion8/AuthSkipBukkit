package com.gmail.necnionch.myplugin.authskip.bukkit.util;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.BaseMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Arrays;

public class MySessionService extends YggdrasilMinecraftSessionService {

    private Register register;

    public MySessionService(YggdrasilAuthenticationService authenticationService) {
        super(authenticationService);
    }

    protected void setRegister(Register register) {
        this.register = register;
    }

    protected Register getRegister() {
        return register;
    }

    public boolean register() {
        if (register != null)
            return register.register();
        return false;
    }

    public boolean unregister() {
        if (register != null)
            return register.unregister();
        return false;
    }


    // FIXME: 1.8.8 には InetAddress 引数がない
    @Override
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
        return super.hasJoinedServer(user, serverId, address);
    }


    public static MySessionService create(Object minecraftServer) throws RuntimeException {
        Field sessionServiceField = Arrays.stream(minecraftServer.getClass().getDeclaredFields())
                .filter(f -> MinecraftSessionService.class.equals(f.getType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to find session service field in MinecraftServer class"));

        Object sessionService;
        try {
            sessionServiceField.setAccessible(true);
            sessionService = sessionServiceField.get(minecraftServer);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to get session service (reflection failed)");
        }

        if (sessionService == null) {
            throw new RuntimeException("Unable to get session service (offline mode?)");
        }

        if (!(sessionService instanceof BaseMinecraftSessionService)) {
            throw new RuntimeException("Unable to get session service (unknown super)");
        }

        MySessionService mySessionService;
        try {
            AuthenticationService authService = ((BaseMinecraftSessionService) sessionService).getAuthenticationService();
            mySessionService = new MySessionService((YggdrasilAuthenticationService) authService);
        } catch (Exception e) {
            throw new RuntimeException("Unable to setup NMSSessionService", e);
        }

        mySessionService.setRegister(new Register() {
            @Override
            public boolean register() {
                try {
                    sessionServiceField.set(minecraftServer, mySessionService);
                } catch (IllegalAccessException e) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean unregister() {
                try {
                    sessionServiceField.set(minecraftServer, sessionService);
                } catch (IllegalAccessException e) {
                    return false;
                }
                return true;
            }
        });
        return mySessionService;
    }


    public interface Register {
        boolean register();
        boolean unregister();
    }

}
