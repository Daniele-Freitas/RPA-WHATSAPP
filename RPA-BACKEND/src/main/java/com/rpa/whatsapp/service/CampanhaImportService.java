package com.rpa.whatsapp.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
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
@RequiredArgsConstructor
public class CampanhaImportService {

  private final CampanhaRepository campanhaRepository;
  private final ContatoRepository contatoRepository;
  private final ContatoService contatoService;
  private final RabbitMQSender rabbitMQSender;
  private final CsvContatoParser csvContatoParser;

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

    List<Contato> contatosSalvos = contatoRepository.saveAll(
        contatos.stream()
            .map(contato -> contatoService.criarContato(contato, campanhaSalva))
            .toList());

    rabbitMQSender.publishContatos(contatosSalvos);

    return new CampanhaCsvImportResponse(
        campanhaSalva.getId(),
        contatos.size(),
        contatosSalvos.size(),
        List.of());
  }
}
