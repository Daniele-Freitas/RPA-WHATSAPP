package com.rpa.whatsapp.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampanhaCsvImportResponse {
  private UUID id;
  private int total;
  private int importados;
  private List<String> telefonesInvalidos;
}
