package com.feral.vpnbot.repositories;

import com.feral.vpnbot.models.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, Long> {
    List<Account> findAllByUser_Server_id(Long serverId);

    long countByUser_Server_Id(Long serverId);
}
