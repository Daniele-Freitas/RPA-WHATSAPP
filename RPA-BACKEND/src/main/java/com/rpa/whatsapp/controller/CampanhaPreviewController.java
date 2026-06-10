package com.rpa.whatsapp.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rpa.whatsapp.dto.CsvPreviewResponse;
import com.rpa.whatsapp.service.CsvContatoParser;
import com.rpa.whatsapp.service.FileConverterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
public class CampanhaPreviewController {

    private final CsvContatoParser csvContatoParser;
    private final FileConverterService fileConverterService;

    @PostMapping(value = "/preview-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Pré-visualizar dados de contatos",
        description = "Recebe um arquivo (CSV, Excel, PDF ou Imagem), converte para uma estrutura padrão em memória e retorna uma amostra das primeiras linhas para validação visual e heurística no Frontend."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Amostra extraída e gerada com sucesso.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CsvPreviewResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Arquivo inválido, vazio, formato não suportado ou sem cabeçalho.",
            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Arquivo vazio ou não enviado."))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Erro interno no servidor ao processar a leitura do arquivo ou falha na API de OCR.",
            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Erro ao processar a leitura do arquivo."))
        )
    })
    public ResponseEntity<?> previewCsv(
            @Parameter(description = "Arquivo contendo a lista de contatos (Formatos suportados: CSV, XLSX, XLS, PDF, PNG, JPG, JPEG)", required = true)
            @RequestPart("arquivo") MultipartFile arquivo) {
        
        try {
            if (arquivo == null || arquivo.isEmpty()) {
                return ResponseEntity.badRequest().body("Arquivo vazio ou não enviado.");
            }

            String conteudoCsv = fileConverterService.converterParaCsv(arquivo);
            CsvPreviewResponse response = csvContatoParser.preview(conteudoCsv, 5);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException ex) {
            // O usuário mandou um arquivo inválido ou o CSV está quebrado
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IOException ex) {
            // Falha ao ler os bytes do arquivo (Problema de I/O)
            return ResponseEntity.internalServerError().body("Erro ao processar a leitura do arquivo.");
        } catch (Exception ex) {
            // Um bug inesperado no nosso código ou falha na API de OCR
            return ResponseEntity.internalServerError().body("Ocorreu um erro interno: " + ex.getMessage());
        }
    }
}