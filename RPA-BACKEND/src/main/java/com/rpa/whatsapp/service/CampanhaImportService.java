package com.rpa.whatsapp.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime; // <-- Novo Import necessário
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
  private final FileConverterService fileConverterService;

  @Transactional 
  public CampanhaCsvImportResponse importar(MultipartFile arquivo, CampanhaCsvImportRequest request) throws IOException, IllegalArgumentException {
    String conteudoCsv = fileConverterService.converterParaCsv(arquivo);
    List<ContatoRequest> contatos = csvContatoParser.parse(conteudoCsv, request);

    if (contatos.isEmpty()) {
      throw new IllegalArgumentException("CSV sem contatos válidos");
    }

    List<String> telefonesInvalidos = contatoService.listarTelefonesInvalidos(contatos);
    if (!telefonesInvalidos.isEmpty()) {
      return new CampanhaCsvImportResponse(null, contatos.size(), 0, telefonesInvalidos);
    }

    // ==========================================
    // INÍCIO DA LIMPEZA E SUBSTITUIÇÃO DA MENSAGEM
    // ==========================================
    String templateLimpo = request.getMensagem() != null ? request.getMensagem() : "";
    
    // 1. Remove o caractere invisível (Zero-Width Space) do Angular
    templateLimpo = templateLimpo.replace("\u200b", "");
    
    // 2. Substitui a saudação pelo horário atual do servidor
    templateLimpo = templateLimpo.replace("{{saudacao_tempo}}", obterSaudacao());
    // ==========================================

    Campanha campanha = Campanha.builder()
        .nome(request.getNome())
        .mensagemTemplate(templateLimpo) // <-- Salvamos o template limpo e processado!
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

  // Método auxiliar para decidir a saudação baseada na hora atual
  private String obterSaudacao() {
    int hora = LocalTime.now().getHour();
    if (hora < 12) return "Bom dia";
    if (hora < 18) return "Boa tarde";
    return "Boa noite";
  }
}