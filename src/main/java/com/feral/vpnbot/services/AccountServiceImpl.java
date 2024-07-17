package com.feral.vpnbot.services;

import com.feral.vpnbot.libs.ServerLib;
import com.feral.vpnbot.models.Account;
import com.feral.vpnbot.models.Server;
import com.feral.vpnbot.models.User;
import com.feral.vpnbot.repositories.AccountRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Value("${server.host}")
    String host;
    @Value("${server.password}")
    String password;
    @Value("${server.public_key}")
    String publicKey;
    @Value("${server.private_key}")
    String privateKey;


    @Autowired
    private ServerLib serverLib;

    @Autowired
    private AccountRepository accountRepository;

    @SneakyThrows
    public File createConfig(User user) {
        Server server = user.getServer();
        Account account = createAccount(user);

        String accountTemplate = generateAccountTemplateInConfig(account.getPublicKey(), account.getClientIp());

        serverLib.addToConfigAndRebootWG(server.getIp(), server.getPassword(), accountTemplate);

        return generateClientConfigFile(account, server);
    }

    private Account createAccount(User user) throws IOException {
        Server server = user.getServer();
        Account account = new Account();
        long userAccountsCount = accountRepository.countByUser_Server_Id(server.getId()) + 2;
        String username = user.getId() + "_" + userAccountsCount;
        String ip = "10.0.0." + userAccountsCount + "/32";
        account.setName(username);
        // public then private keys
        List<String> keys = serverLib.generateKeyPairs(server.getIp(), server.getPassword(), account.getName());
        account.setUser(user);
        account.setName(username);
        account.setClientIp(ip);
        account.setPublicKey(keys.getFirst());
        account.setPrivateKey(keys.getLast());
        accountRepository.save(account);
        return account;
    }

    private String generateAccountTemplateInConfig(String publicKey, String ip) {
        StringBuilder template = new StringBuilder("\\n \\n");
        template.append("[Peer]\\n");
        template.append("PublicKey = %s\\n".formatted(publicKey));
        template.append("AllowedIps = %s\\n".formatted(ip));
        return template.toString();
    }

    public File generateClientConfigFile(Account account, Server server) {
        String clientConfig = "[Interface]\n" +
                "PrivateKey = %s\n" +
                "Address = %s\n" +
                "DNS = 8.8.8.8\n" +
                "\n" +
                "[Peer]\n" +
                "PublicKey = %s\n" +
                "Endpoint = %s:51830\n" +
                "AllowedIPs = 0.0.0.0/0\n" +
                "PersistentKeepalive = 20";

        String formattedConfig = String.format(clientConfig, account.getPrivateKey(), account.getClientIp(), server.getWgPublicKey(), server.getIp());

        try {
            File tempFile = File.createTempFile("userConfig", ".conf");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(formattedConfig);
            writer.close();
            return tempFile;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

}
