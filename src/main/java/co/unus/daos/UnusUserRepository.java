package co.unus.daos;

import co.unus.models.UnusUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

public interface UnusUserRepository<T, ID extends Serializable> extends JpaRepository<UnusUser, String> { }
