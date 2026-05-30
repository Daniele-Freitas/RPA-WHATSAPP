package com.rpa.whatsapp.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.rpa.whatsapp.domain.Campanha;

public interface CampanhaRepository extends JpaRepository<Campanha, UUID> {}
