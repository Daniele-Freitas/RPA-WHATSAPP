package com.rpa.whatsapp.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Operation(summary = "Preview CSV", description = "Retorna colunas e amostras do CSV.")
  @ApiResponse(responseCode = "200", description = "Preview gerado",
    content = @Content(schema = @Schema(implementation = CampanhaCsvPreviewResponse.class)))
  public ResponseEntity<CampanhaCsvPreviewResponse> previewCsv(
      @RequestPart("arquivo") @io.swagger.v3.oas.annotations.Parameter(description = "Arquivo CSV para análise") MultipartFile arquivo) {
    if (arquivo == null || arquivo.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    CampanhaCsvPreviewResponse response = csvPreviewService.preview(arquivo, 5);
    return ResponseEntity.ok(response);
  }
}
