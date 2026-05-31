package com.rpa.whatsapp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.rpa.whatsapp.dto.CampanhaCsvPreviewResponse;

@Service
public class CsvPreviewService {

  public CampanhaCsvPreviewResponse preview(MultipartFile arquivo, int limite) {
    if (arquivo == null || arquivo.isEmpty()) {
      throw new IllegalArgumentException("Arquivo CSV e obrigatorio");
    }

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8));
        CSVParser parser = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .build()
            .parse(reader)) {

      List<String> colunas = new ArrayList<>(parser.getHeaderMap().keySet());
      List<Map<String, String>> amostras = new ArrayList<>();

      for (CSVRecord record : parser) {
        if (amostras.size() >= limite) {
          break;
        }

        Map<String, String> linha = new LinkedHashMap<>();
        for (String coluna : colunas) {
          linha.put(coluna, obterValor(record, coluna));
        }
        amostras.add(linha);
      }

      return new CampanhaCsvPreviewResponse(colunas, amostras);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Falha ao ler arquivo CSV", ex);
    }
  }

  private String obterValor(CSVRecord record, String coluna) {
    if (!record.isMapped(coluna)) {
      return null;
    }

    String valor = record.get(coluna);
    if (valor == null) {
      return null;
    }

    String normalizado = valor.trim();
    return normalizado.isBlank() ? null : normalizado;
  }
}
