package com.feral.vpnbot.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "servers")
@Data
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String ip;

    private String password;

    private String wgPublicKey;

    private String wgPrivateKey;
}
