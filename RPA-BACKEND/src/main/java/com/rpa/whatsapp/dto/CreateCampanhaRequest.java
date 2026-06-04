package com.rpa.whatsapp.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para criar campanha manualmente")
public class CreateCampanhaRequest {
  @Schema(example = "Campanha Manual")
  private String nome;

  @Schema(example = "Olá {{nome}}")
  private String mensagem;

  @Schema(description = "Lista de contatos a serem adicionados à campanha")
  private List<ContatoRequest> contatos;
}
