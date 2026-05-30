package com.rpa.whatsapp.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.rpa.whatsapp.domain.Contato;

public interface ContatoRepository extends JpaRepository<Contato, UUID> {}
