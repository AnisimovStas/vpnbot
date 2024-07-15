package com.feral.vpnbot.repositories;

import com.feral.vpnbot.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
