package com.rpa.whatsapp.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para importar campanha via CSV")
public class CampanhaCsvImportRequest {
  @Schema(example = "Campanha Promoção")
  private String nome;

  @Schema(example = "Olá {{primeiro_nome}}, aproveite nossa promoção")
  private String mensagem;

  @Schema(description = "Nomes das colunas que podem conter telefones (aceita múltiplas colunas)", example = "[\"Telefone\", \"Telefone2\"]")
  private java.util.List<String> colunaTelefones;

  @Schema(example = "Nome", description = "Nome da coluna que contém o nome")
  private String colunaNome;

  @Schema(description = "Mapeamento de variáveis para colunas do CSV", example = "{\"primeiro_nome\": \"Nome\"}")
  private Map<String, String> colunasVariaveis;
}
