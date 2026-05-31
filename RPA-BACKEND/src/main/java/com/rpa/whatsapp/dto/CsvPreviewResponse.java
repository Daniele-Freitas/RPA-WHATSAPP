package com.rpa.whatsapp.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsvPreviewResponse {
  private List<String> colunas;
  private List<Map<String, String>> amostras;
  private int total;
}
