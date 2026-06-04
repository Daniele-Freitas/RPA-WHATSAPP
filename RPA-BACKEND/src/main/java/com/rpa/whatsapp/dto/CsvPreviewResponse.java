package com.rpa.whatsapp.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta genérica de preview de CSV")
public class CsvPreviewResponse {
  @Schema(description = "Nomes das colunas detectadas")
  private List<String> colunas;

  @Schema(description = "Linhas de amostra do CSV")
  private List<Map<String, String>> amostras;

  @Schema(description = "Total de linhas no CSV", example = "123")
  private int total;
}
