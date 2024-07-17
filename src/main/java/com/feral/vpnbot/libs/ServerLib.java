package com.feral.vpnbot.libs;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ServerLib {

    public String SSHConnect(String host, String password, List<String> commands) throws IOException {
        String username = "root";
        int port = 22;
        long defaultTimeoutSeconds = 1;

        SshClient client = SshClient.setUpDefaultClient();
        client.start();

        try (ClientSession session = client.connect(username, host, port)
                .verify(defaultTimeoutSeconds, TimeUnit.SECONDS).getSession()) {
            session.addPasswordIdentity(password);
            session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);

            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                 ClientChannel channel = session.createChannel(Channel.CHANNEL_SHELL)) {
                channel.setOut(responseStream);
                try {
                    channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
                    try (OutputStream pipedIn = channel.getInvertedIn()) {
                        for (String command : commands) {
                            pipedIn.write((command + "\n").getBytes());
                            pipedIn.flush();
                        }
                    }

                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                            TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds));
                    String responseString = new String(responseStream.toByteArray());
                    return responseString;
                } finally {
                    channel.close(false);
                }
            }
        } finally {
            client.stop();
        }
    }


    public List<String> generateKeyPairs(String host, String password, String username) throws IOException {
        log.info("username in generateKeyPairs is: {}", username);
        String generate = "wg genkey | tee /etc/wireguard/" + username + "_privatekey | wg pubkey | tee /etc/wireguard/" + username + "_publickey";
        String getPubKey = "cat /etc/wireguard/" + username + "_publickey";
        String getPrivateKey = "cat /etc/wireguard/" + username + "_privatekey";

        String result = SSHConnect(host, password, List.of(generate, getPubKey, getPrivateKey));
//        String result = SSHConnect(host, password, List.of(getPubKey, getPrivateKey));
        return extractKeys(result);
    }

    public void addToConfigAndRebootWG(String host, String password, String accountTemplate) throws IOException {
        log.info("account template is: {}", accountTemplate);
        String addAccountToConfig = "echo -e \"%s\" >> /etc/wireguard/wg0.conf".formatted(accountTemplate);
        String rebootWG = "systemctl restart wg-quick@wg0";
        SSHConnect(host, password, List.of(addAccountToConfig, rebootWG));
    }

    /*
    return list<public,private> keys
     */
    public static List<String> extractKeys(String input) {
        List<String> keys = new ArrayList<>();
        String[] inputArray = input.split("\n");
        String publicKey = inputArray[inputArray.length - 4];
        String privateKey = inputArray[inputArray.length - 2];

        keys.add(publicKey);
        keys.add(privateKey);
        return keys;
    }


}

