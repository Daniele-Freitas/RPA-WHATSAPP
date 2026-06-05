package com.rpa.whatsapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rpa.whatsapp.domain.Campanha;
import com.rpa.whatsapp.domain.CampanhaStatus;
import com.rpa.whatsapp.domain.Contato;
import com.rpa.whatsapp.dto.CreateCampanhaRequest;
import com.rpa.whatsapp.dto.ContatoRequest;
import com.rpa.whatsapp.repository.CampanhaRepository;
import com.rpa.whatsapp.repository.ContatoRepository;
import com.rpa.whatsapp.service.ContatoService;
import com.rpa.whatsapp.service.RabbitMQSender;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
@Tag(name = "Campanhas", description = "Endpoints para gerenciamento e disparo de mensagens no WhatsApp")
public class CampanhaController {

  private final CampanhaRepository campanhaRepository;
  private final ContatoRepository contatoRepository;
  private final ContatoService contatoService;
  private final RabbitMQSender rabbitMQSender;

  /**
   * POST /api/campanhas
   * 
   * Cria uma nova campanha com contatos e publica na fila do RabbitMQ
   * 
   * Body (JSON):
   * {
   *   "nome": "Campanha Teste - Mai 2026",
    *   "mensagem": "{{saudacao}}, {{primeiro_nome}} tudo bem?",
   *   "contatos": [
   *     {
    *       "nome": "Maria Silva",
     *       "telefone": "5511999999999",
     *       "variaveis": {
     *         "primeiro_nome": "Maria"
     *       }
   *     },
   *     {
    *       "nome": "João Souza",
     *       "telefone": "5511888888888",
     *       "variaveis": {
     *         "primeiro_nome": "João"
     *       }
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
  @Operation(summary = "Criar nova campanha", description = "Recebe uma lista de contatos e um template, e enfileira no RabbitMQ para processamento.")
  public ResponseEntity<Map<String, Object>> criar(@RequestBody CreateCampanhaRequest request) {
    if (request == null || request.getNome() == null || request.getNome().isBlank()) {
      return ResponseEntity.badRequest()
          .body(Map.of("erro", "Nome da campanha é obrigatório"));
    }

    if (request.getMensagem() == null || request.getMensagem().isBlank()) {
      return ResponseEntity.badRequest()
          .body(Map.of("erro", "Mensagem da campanha é obrigatória"));
    }

    List<ContatoRequest> contatosRequest = request.getContatos() == null
        ? List.of()
        : request.getContatos();

    if (contatosRequest.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(Map.of("erro", "Lista de contatos é obrigatória"));
    }

    List<String> telefonesInvalidos = contatoService.listarTelefonesInvalidos(contatosRequest);
    if (!telefonesInvalidos.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(Map.of(
              "erro", "Telefones inválidos",
              "telefonesInvalidos", telefonesInvalidos));
    }

    Campanha campanha = Campanha.builder()
      .nome(request.getNome())
      .mensagemTemplate(request.getMensagem())
      .status(CampanhaStatus.PENDENTE)
      .dataCriacao(LocalDateTime.now())
      .build();

    Campanha campanhaSalva = campanhaRepository.save(campanha);

    List<Contato> contatos = contatosRequest.stream()
      .map(contato -> contatoService.criarContato(contato, campanhaSalva))
        .toList();

    List<Contato> contatosSalvos = contatoRepository.saveAll(contatos);
    rabbitMQSender.publishContatos(contatosSalvos);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of(
            "id", campanhaSalva.getId(),
            "contatos", contatosSalvos.size()));
  }
}
