package com.rpa.whatsapp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.rpa.whatsapp.dto.CampanhaCsvImportRequest;
import com.rpa.whatsapp.dto.ContatoRequest;

@Service
public class CsvContatoParser {

  public List<ContatoRequest> parse(MultipartFile arquivo, CampanhaCsvImportRequest config) {
    if (arquivo == null || arquivo.isEmpty()) {
      throw new IllegalArgumentException("Arquivo CSV é obrigatório");
    }

    if (config == null) {
      throw new IllegalArgumentException("Configuração do CSV é obrigatória");
    }

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8));
        CSVParser parser = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .build()
            .parse(reader)) {

      validarColunas(parser.getHeaderMap(), config);

      List<ContatoRequest> contatos = new ArrayList<>();
      for (CSVRecord record : parser) {
        contatos.add(mapearContato(record, config));
      }

      return contatos;
    } catch (IOException ex) {
      throw new IllegalArgumentException("Falha ao ler arquivo CSV", ex);
    }
  }

  private void validarColunas(Map<String, Integer> headers, CampanhaCsvImportRequest config) {
    validarColunaObrigatoria(headers, config.getColunaTelefone(), "colunaTelefone");

    if (config.getColunaNome() != null && !config.getColunaNome().isBlank()) {
      validarColunaObrigatoria(headers, config.getColunaNome(), "colunaNome");
    }

    if (config.getColunasVariaveis() != null) {
      for (Map.Entry<String, String> entry : config.getColunasVariaveis().entrySet()) {
        if (entry.getValue() == null || entry.getValue().isBlank()) {
          continue;
        }
        validarColunaObrigatoria(headers, entry.getValue(), "colunasVariaveis");
      }
    }
  }

  private void validarColunaObrigatoria(Map<String, Integer> headers, String coluna, String campo) {
    if (coluna == null || coluna.isBlank()) {
      throw new IllegalArgumentException("Campo obrigatório: " + campo);
    }

    if (!headers.containsKey(coluna)) {
      throw new IllegalArgumentException("Coluna não encontrada no CSV: " + coluna);
    }
  }

  private ContatoRequest mapearContato(CSVRecord record, CampanhaCsvImportRequest config) {
    String telefone = obterValor(record, config.getColunaTelefone());
    String nome = obterValor(record, config.getColunaNome());

    Map<String, String> variaveis = new HashMap<>();
    if (config.getColunasVariaveis() != null) {
      for (Map.Entry<String, String> entry : config.getColunasVariaveis().entrySet()) {
        String valor = obterValor(record, entry.getValue());
        if (valor != null) {
          variaveis.put(entry.getKey(), valor);
        }
      }
    }

    return new ContatoRequest(nome, telefone, variaveis);
  }

  private String obterValor(CSVRecord record, String coluna) {
    if (coluna == null || coluna.isBlank()) {
      return null;
    }

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
