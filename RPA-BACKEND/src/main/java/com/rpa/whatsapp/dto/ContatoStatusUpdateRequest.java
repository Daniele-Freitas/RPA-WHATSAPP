package com.rpa.whatsapp.dto;

import com.rpa.whatsapp.domain.StatusEnvio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Atualização do status de envio do contato")
public class ContatoStatusUpdateRequest {
  @Schema(description = "Novo status do envio", example = "SUCESSO")
  private StatusEnvio statusEnvio;
}
