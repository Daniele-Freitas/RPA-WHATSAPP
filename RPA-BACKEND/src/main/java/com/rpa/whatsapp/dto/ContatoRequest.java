package com.rpa.whatsapp.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoRequest {
  private String nome;
  private String telefone;
  private Map<String, String> variaveis;
}
