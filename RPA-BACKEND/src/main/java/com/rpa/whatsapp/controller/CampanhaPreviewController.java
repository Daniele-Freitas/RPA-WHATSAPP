package com.rpa.whatsapp.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.rpa.whatsapp.dto.CampanhaCsvPreviewResponse;
import com.rpa.whatsapp.service.CsvPreviewService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
public class CampanhaPreviewController {

  private final CsvPreviewService csvPreviewService;

  /**
   * POST /api/campanhas/preview-csv
   * 
   * Form-data:
   * - arquivo: (file) contatos.csv
   * 
   * Response (200 OK):
   * {
   *   "colunas": ["Nome", "Telefone", "Projeto"],
   *   "amostras": [
   *     { "Nome": "Maria", "Telefone": "5511999999999", "Projeto": "XYZ" }
   *   ]
   * }
   */
  @PostMapping(value = "/preview-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CampanhaCsvPreviewResponse> previewCsv(
      @RequestPart("arquivo") MultipartFile arquivo) {
    if (arquivo == null || arquivo.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    CampanhaCsvPreviewResponse response = csvPreviewService.preview(arquivo, 5);
    return ResponseEntity.ok(response);
  }
}
