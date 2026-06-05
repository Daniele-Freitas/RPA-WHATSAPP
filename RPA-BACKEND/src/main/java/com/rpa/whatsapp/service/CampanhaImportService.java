package com.rpa.whatsapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects; // <-- Import para o filtro
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- Import para a transação
import org.springframework.web.multipart.MultipartFile;
import com.rpa.whatsapp.domain.Campanha;
import com.rpa.whatsapp.domain.CampanhaStatus;
import com.rpa.whatsapp.domain.Contato;
import com.rpa.whatsapp.dto.CampanhaCsvImportRequest;
import com.rpa.whatsapp.dto.CampanhaCsvImportResponse;
import com.rpa.whatsapp.dto.ContatoRequest;
import com.rpa.whatsapp.repository.CampanhaRepository;
import com.rpa.whatsapp.repository.ContatoRepository;
import lombok.RequiredArgsConstructor;

@Service
@SuppressWarnings("null") 
@RequiredArgsConstructor
public class CampanhaImportService {

  private final CampanhaRepository campanhaRepository;
  private final ContatoRepository contatoRepository;
  private final ContatoService contatoService;
  private final RabbitMQSender rabbitMQSender;
  private final CsvContatoParser csvContatoParser;

  @Transactional // <-- Garante que não teremos dados pela metade no banco
  public CampanhaCsvImportResponse importar(MultipartFile arquivo, CampanhaCsvImportRequest request) {
    List<ContatoRequest> contatos = csvContatoParser.parse(arquivo, request);

    if (contatos.isEmpty()) {
      throw new IllegalArgumentException("CSV sem contatos válidos");
    }

    List<String> telefonesInvalidos = contatoService.listarTelefonesInvalidos(contatos);
    if (!telefonesInvalidos.isEmpty()) {
      return new CampanhaCsvImportResponse(null, contatos.size(), 0, telefonesInvalidos);
    }

    Campanha campanha = Campanha.builder()
        .nome(request.getNome())
        .mensagemTemplate(request.getMensagem())
        .status(CampanhaStatus.PENDENTE)
        .dataCriacao(LocalDateTime.now())
        .build();

    Campanha campanhaSalva = campanhaRepository.save(campanha);

    // 1. Primeiro geramos e guardamos a lista numa variável
    List<Contato> contatosParaSalvar = contatos.stream()
        .map(contato -> contatoService.criarContato(contato, campanhaSalva))
        .filter(Objects::nonNull)
        .toList();

    // 2. Depois passamos a variável para o repositório
    List<Contato> contatosSalvos = contatoRepository.saveAll(contatosParaSalvar);

    rabbitMQSender.publishContatos(contatosSalvos);

    return new CampanhaCsvImportResponse(
        campanhaSalva.getId(),
        contatos.size(),
        contatosSalvos.size(),
        List.of());
  }
}