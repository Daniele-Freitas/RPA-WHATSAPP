package com.rpa.whatsapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rpa.whatsapp.domain.Campanha;
import com.rpa.whatsapp.domain.CampanhaStatus;
import com.rpa.whatsapp.domain.Contato;
import com.rpa.whatsapp.domain.StatusEnvio;
import com.rpa.whatsapp.dto.CreateCampanhaRequest;
import com.rpa.whatsapp.dto.ContatoRequest;
import com.rpa.whatsapp.repository.CampanhaRepository;
import com.rpa.whatsapp.repository.ContatoRepository;
import com.rpa.whatsapp.service.RabbitMQSender;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
public class CampanhaController {

  private final CampanhaRepository campanhaRepository;
  private final ContatoRepository contatoRepository;
  private final RabbitMQSender rabbitMQSender;

  /**
   * POST /api/campanhas
   * 
   * Cria uma nova campanha com contatos e publica na fila do RabbitMQ
   * 
   * Body (JSON):
   * {
   *   "nome": "Campanha Teste - Mai 2026",
   *   "contatos": [
   *     {
   *       "telefone": "5511999999999",
   *       "mensagemFormatada": "Olá! Esta é uma mensagem de teste 1."
   *     },
   *     {
   *       "telefone": "5511888888888",
   *       "mensagemFormatada": "Olá! Esta é uma mensagem de teste 2."
   *     }
   *   ]
   * }
   * 
   * Response (201 Created):
   * {
   *   "id": "550e8400-e29b-41d4-a716-446655440000"
   * }
   */
  @PostMapping
  public ResponseEntity<Map<String, UUID>> criar(@RequestBody CreateCampanhaRequest request) {
    Campanha campanha = Campanha.builder()
        .nome(request.getNome())
        .status(CampanhaStatus.PENDENTE)
        .dataCriacao(LocalDateTime.now())
        .build();

    Campanha campanhaSalva = campanhaRepository.save(campanha);

    List<ContatoRequest> contatosRequest = request.getContatos() == null
        ? List.of()
        : request.getContatos();

    List<Contato> contatos = contatosRequest.stream()
        .map(contato -> Contato.builder()
            .telefone(contato.getTelefone())
            .mensagemFormatada(contato.getMensagemFormatada())
            .statusEnvio(StatusEnvio.PENDENTE)
            .campanha(campanhaSalva)
            .build())
        .toList();

    List<Contato> contatosSalvos = contatoRepository.saveAll(contatos);
    rabbitMQSender.publishContatos(contatosSalvos);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("id", campanhaSalva.getId()));
  }
}
