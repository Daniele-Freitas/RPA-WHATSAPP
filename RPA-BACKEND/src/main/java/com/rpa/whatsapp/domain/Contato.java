package com.rpa.whatsapp.domain;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import java.util.Map;
import com.rpa.whatsapp.persistence.JsonMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contatos")
public class Contato {

  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column
  private String nome;

  @Column(nullable = false)
  private String telefone;

  @Convert(converter = JsonMapConverter.class)
  @Column(columnDefinition = "jsonb")
  private Map<String, String> variaveis;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusEnvio statusEnvio;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campanha_id", nullable = false)
  private Campanha campanha;
}
