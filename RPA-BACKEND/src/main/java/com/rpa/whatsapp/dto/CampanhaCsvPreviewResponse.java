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
@Schema(description = "Preview do CSV com colunas e amostras")
public class CampanhaCsvPreviewResponse {
  @Schema(description = "Nomes das colunas detectadas")
  private List<String> colunas;

  @Schema(description = "Linhas de amostra (map: coluna -> valor)")
  private List<Map<String, String>> amostras;
}
