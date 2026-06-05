package com.rpa.whatsapp.dto;

import java.util.Map;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representação de um contato")
public class ContatoRequest {
  @Schema(example = "João")
  private String nome;

  @Schema(description = "Lista de telefones do contato (pode conter múltiplas colunas do CSV)", example = "[\"5511999999999\", \"5511888888888\"]")
  private List<String> telefones;

  @Schema(description = "Mapa de variáveis personalizáveis (nome da variável -> coluna CSV)")
  private Map<String, String> variaveis;
}
