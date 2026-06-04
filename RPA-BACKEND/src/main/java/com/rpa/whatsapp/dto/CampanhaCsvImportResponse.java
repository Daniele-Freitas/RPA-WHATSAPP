package com.rpa.whatsapp.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta da importação CSV")
public class CampanhaCsvImportResponse {
  @Schema(description = "ID da campanha criada")
  private UUID id;

  @Schema(description = "Total de registros no CSV", example = "100")
  private int total;

  @Schema(description = "Quantidade efetivamente importada", example = "95")
  private int importados;

  @Schema(description = "Lista de telefones considerados inválidos")
  private List<String> telefonesInvalidos;
}
