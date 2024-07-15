package com.feral.vpnbot.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "accounts")
@Data
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String clientIp;

    private String publicKey;

    private String privateKey;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
