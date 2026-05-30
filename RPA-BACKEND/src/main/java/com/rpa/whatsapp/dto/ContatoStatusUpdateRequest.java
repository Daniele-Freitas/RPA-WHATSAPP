package com.rpa.whatsapp.dto;

import com.rpa.whatsapp.domain.StatusEnvio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoStatusUpdateRequest {
  private StatusEnvio statusEnvio;
}
