package com.rpa.whatsapp.controller;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.rpa.whatsapp.domain.Contato;
import com.rpa.whatsapp.dto.ContatoStatusUpdateRequest;
import com.rpa.whatsapp.repository.ContatoRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contatos")
@RequiredArgsConstructor
public class ContatoController {

  private final ContatoRepository contatoRepository;

  /**
   * PATCH /api/contatos/{id}/status
   * 
   * Body (JSON):
   * {
   *   "statusEnvio": "SUCESSO"
   * }
   * 
   * Response (204 No Content)
   */
  @PatchMapping("/{id}/status")
  public ResponseEntity<Void> atualizarStatus(
      @PathVariable UUID id,
      @RequestBody ContatoStatusUpdateRequest request) {
    if (request == null || request.getStatusEnvio() == null) {
      return ResponseEntity.badRequest().build();
    }

    Contato contato = contatoRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado"));

    contato.setStatusEnvio(request.getStatusEnvio());
    contatoRepository.save(contato);

    return ResponseEntity.noContent().build();
  }
}
