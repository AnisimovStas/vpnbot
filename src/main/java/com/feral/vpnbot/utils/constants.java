package com.feral.vpnbot.utils;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class constants {
    public static final String WELCOME_MESSAGE = "Welcome to vpn bot, use button keyboard to navigate";

    public static final String UNRECOGNIZED_COMMAND = "Sorry, i dont understand you, try something different";
    public static final String TBD_LATER = "TBD later, there is no such functional atm";

    public static final String ALREADY_REGISTERED = "You are already registered :) \n \n Use button keyboard to navigate";
    public static final String USER_NOT_FOUND = "Sorry, for some reason we don't find your user data, please type /start to registration";

 
    public static final String ACCOUNT_LIST_EMPTY = "You don't have any account, create it first by clicking 'create new account' button im buttom menu";
    public static final String ACCOUNT_LIST_START = "Here is the list of your accounts:";
}
