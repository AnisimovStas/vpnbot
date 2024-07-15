package com.feral.vpnbot.utils;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class constants {
    public static final String WELCOME_MESSAGE = "Welcome to vpn bot, use button keyboard to navigate";
    public static final String UNRECOGNIZED_COMMAND = "Sorry, i dont understand you, try something different";
    public static final String ALREADY_REGISTERED = "You are already registered :) \n \n Use button keyboard to navigate";
}
