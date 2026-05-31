package com.rpa.whatsapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.rpa.whatsapp.dto.CampanhaCsvImportRequest;
import com.rpa.whatsapp.dto.CampanhaCsvImportResponse;
import com.rpa.whatsapp.service.CampanhaImportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
public class CampanhaImportController {

  private final CampanhaImportService campanhaImportService;

  /**
   * POST /api/campanhas/importar-csv
   * 
   * Form-data:
   * - arquivo: (file) contatos.csv
   * - config: (application/json)
   * {
   *   "nome": "Campanha CSV",
   *   "mensagem": "{{saudacao}}, {{primeiro_nome}} tudo bem?",
   *   "colunaTelefone": "Telefone",
   *   "colunaNome": "Nome",
   *   "colunasVariaveis": {
   *     "primeiro_nome": "PrimeiroNome",
   *     "empreendimento": "Projeto"
   *   }
   * }
   */
  @PostMapping(value = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CampanhaCsvImportResponse> importarCsv(
      @RequestPart("arquivo") MultipartFile arquivo,
      @RequestPart("config") CampanhaCsvImportRequest request) {
    if (request == null || request.getNome() == null || request.getNome().isBlank()) {
      return ResponseEntity.badRequest()
          .body(new CampanhaCsvImportResponse(null, 0, 0, List.of("Nome da campanha é obrigatório")));
    }

    if (request.getMensagem() == null || request.getMensagem().isBlank()) {
      return ResponseEntity.badRequest()
          .body(new CampanhaCsvImportResponse(null, 0, 0, List.of("Mensagem é obrigatória")));
    }

    CampanhaCsvImportResponse response = campanhaImportService.importar(arquivo, request);
    if (response.getTelefonesInvalidos() != null && !response.getTelefonesInvalidos().isEmpty()) {
      return ResponseEntity.badRequest().body(response);
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
