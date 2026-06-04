package com.rpa.whatsapp.dto;

import java.util.Map;
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

  @Schema(example = "5511999999999", description = "Telefone no formato internacional sem sinais")
  private String telefone;

  @Schema(description = "Mapa de variáveis personalizáveis (nome da variável -> coluna CSV)")
  private Map<String, String> variaveis;
}
