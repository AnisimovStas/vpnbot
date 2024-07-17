package com.feral.vpnbot.services;

import com.feral.vpnbot.models.User;

import java.io.File;

public interface AccountService {

    public File createConfig(User user);
}
