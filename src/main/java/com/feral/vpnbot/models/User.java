package com.feral.vpnbot.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "server_id")
    private Server server;

    @OneToMany(mappedBy = "user")
    private List<Account> accounts;
}
