package com.rpa.whatsapp.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampanhaCsvImportRequest {
  private String nome;
  private String mensagem;
  private String colunaTelefone;
  private String colunaNome;
  private Map<String, String> colunasVariaveis;
}
