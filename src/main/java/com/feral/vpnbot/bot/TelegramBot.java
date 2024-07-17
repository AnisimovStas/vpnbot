package com.feral.vpnbot.bot;

import com.feral.vpnbot.models.Account;
import com.feral.vpnbot.models.User;
import com.feral.vpnbot.repositories.AccountRepository;
import com.feral.vpnbot.repositories.UserRepository;
import com.feral.vpnbot.services.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.util.List;

import static com.feral.vpnbot.utils.constants.*;

@Slf4j
@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;


    @Value("${bot.token}")
    private String botToken;

    public TelegramBot(@Value("${bot.token}") String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> registerUser(chatId);
                case "create new Account" -> createNewAccount(chatId);
                case "check my Accounts" -> checkAccounts(chatId);
                case "check my Server" -> sendMessageWithMenuKeyBoard(chatId, TBD_LATER);
                case "check my balance" -> sendMessageWithMenuKeyBoard(chatId, TBD_LATER);
                case "/test" -> sendMessageWithMenuKeyBoard(chatId, "test");
                default -> sendMessage(chatId, UNRECOGNIZED_COMMAND);
            }
        }
    }

    private void createNewAccount(long chatId) {

        User user = userRepository.findById(chatId).orElse(null);

        if (user == null) {
            sendMessageWithMenuKeyBoard(chatId, USER_NOT_FOUND);
        } else {
            File clientConfig = accountService.createConfig(user);

            sendMessage(chatId, "here is your config");
            sendDocument(chatId, clientConfig);
            //TODO add instruction
        }


    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    private void checkAccounts(long chatId) {
        User user = userRepository.findById(chatId).orElse(null);
        log.info("we here");
        if (user == null) {
            log.info("if we here user == null");
            sendMessage(chatId, USER_NOT_FOUND);
            return;
        }

        log.info(user.toString());

        List<Account> accounts = accountRepository.findAllByUser_Server_id(user.getServer().getId());

        if (accounts.isEmpty()) {
            sendMessageWithMenuKeyBoard(chatId, ACCOUNT_LIST_EMPTY);
        } else {
            sendMessageWithMenuKeyBoard(chatId, ACCOUNT_LIST_START);
            for (Account account : accounts) {
                log.info(account.toString());
                //TODO add inline keyboard to download config
                sendMessageWithMenuKeyBoard(chatId, account.toString());
            }
        }
    }

    private void registerUser(long chatId) {
        User user = userRepository.findById(chatId).orElse(null);
        if (user == null) {
            user = new User();
            user.setId(chatId);
            userRepository.save(user);
            sendMessageWithMenuKeyBoard(chatId, WELCOME_MESSAGE);
        } else {
            sendMessageWithMenuKeyBoard(chatId, ALREADY_REGISTERED);
        }
    }

    private void sendMessageWithMenuKeyBoard(long chatId, String text) {
        KeyboardRow firstRow = new KeyboardRow();
        KeyboardRow secondRow = new KeyboardRow();
        firstRow.add("create new Account");
        firstRow.add("check my Accounts");
        secondRow.add("check my Server");
        secondRow.add("check my balance");

        SendMessage message = SendMessage // Create a message object
                .builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(ReplyKeyboardMarkup.builder().keyboardRow(firstRow).keyboardRow(secondRow).build())
                .build();

        try {
            telegramClient.execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = SendMessage // Create a message object
                .builder()
                .chatId(chatId)
                .text(text)
                .build();

        try {
            telegramClient.execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }

    }

    private void sendDocument(long chatId, File file) {
        SendDocument doc = SendDocument.builder().chatId(chatId).document(new InputFile(file)).build();
        try {
            telegramClient.execute(doc); // Sending our message object to user
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
    }
}