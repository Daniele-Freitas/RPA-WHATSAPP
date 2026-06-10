package com.rpa.whatsapp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import com.rpa.whatsapp.dto.CampanhaCsvImportRequest;
import com.rpa.whatsapp.dto.CsvPreviewResponse;
import com.rpa.whatsapp.dto.ContatoRequest;

@Service
public class CsvContatoParser {

  public CsvPreviewResponse preview(String conteudoCsv, int maxAmostras) {
    if (conteudoCsv == null || conteudoCsv.isBlank()) {
      throw new IllegalArgumentException("O conteúdo CSV é obrigatório e não pode estar vazio");
    }

    int limite = maxAmostras <= 0 ? 5 : maxAmostras;

    // Em vez de ler um ficheiro físico, lemos diretamente a String em memória
    try (BufferedReader reader = new BufferedReader(new StringReader(conteudoCsv));
         CSVParser parser = CSVFormat.DEFAULT.builder()
             .setHeader()
             .setSkipHeaderRecord(true)
             .setTrim(true)
             .build()
             .parse(reader)) {

      List<String> colunas = new ArrayList<>(parser.getHeaderMap().keySet());
      
      // VALIDAÇÃO CONTRA FALTA DE CABEÇALHO (Fail Fast)
      for (String coluna : colunas) {
        // Se o nome da coluna tem 8 ou mais números seguidos, é um contacto disfarçado de título
        if (coluna.replaceAll("\\D", "").length() >= 8) {
          throw new IllegalArgumentException("Parece que o seu ficheiro não tem uma linha de cabeçalho com os nomes das colunas (Ex: Nome, Telefone). O primeiro contacto (" + coluna + ") foi lido como título. Por favor, adicione os títulos na primeira linha.");
        }
      }

      List<Map<String, String>> amostras = new ArrayList<>();
      int total = 0;

      for (CSVRecord line : parser) {
        total++;
        if (amostras.size() < limite) {
          amostras.add(mapearLinha(line, colunas));
        }
      }

      return new CsvPreviewResponse(colunas, amostras, total);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Falha ao ler o conteúdo CSV", ex);
    }
  }

  public List<ContatoRequest> parse(String conteudoCsv, CampanhaCsvImportRequest config) {
    if (conteudoCsv == null || conteudoCsv.isBlank()) {
      throw new IllegalArgumentException("O conteúdo CSV é obrigatório");
    }

    if (config == null) {
      throw new IllegalArgumentException("A configuração do CSV é obrigatória");
    }

    // Em vez de ler um ficheiro físico, lemos diretamente a String em memória
    try (BufferedReader reader = new BufferedReader(new StringReader(conteudoCsv));
         CSVParser parser = CSVFormat.DEFAULT.builder()
             .setHeader()
             .setSkipHeaderRecord(true)
             .setTrim(true)
             .build()
             .parse(reader)) {

      validarColunas(parser.getHeaderMap(), config);

      List<ContatoRequest> contatos = new ArrayList<>();
      for (CSVRecord line : parser) {
        contatos.add(mapearContato(line, config));
      }

      return contatos;
    } catch (IOException ex) {
      throw new IllegalArgumentException("Falha ao ler o conteúdo CSV", ex);
    }
  }

  private void validarColunas(Map<String, Integer> headers, CampanhaCsvImportRequest config) {
    if (config.getColunaTelefones() == null || config.getColunaTelefones().isEmpty()) {
      throw new IllegalArgumentException("Campo obrigatório: colunaTelefones");
    }

    for (String colunaTel : config.getColunaTelefones()) {
      validarColunaObrigatoria(headers, colunaTel, "colunaTelefones");
    }

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
      throw new IllegalArgumentException("Coluna não encontrada no ficheiro: " + coluna);
    }
  }

  private ContatoRequest mapearContato(CSVRecord line, CampanhaCsvImportRequest config) {
    List<String> telefones = new ArrayList<>();
    for (String colunaTel : config.getColunaTelefones()) {
      String valor = obterValor(line, colunaTel);
      if (valor != null) {
        telefones.add(valor);
      }
    }

    String nome = obterValor(line, config.getColunaNome());

    Map<String, String> variaveis = new HashMap<>();
    if (config.getColunasVariaveis() != null) {
      for (Map.Entry<String, String> entry : config.getColunasVariaveis().entrySet()) {
        String valor = obterValor(line, entry.getValue());
        if (valor != null) {
          variaveis.put(entry.getKey(), valor);
        }
      }
    }

    return new ContatoRequest(nome, telefones, variaveis);
  }

  private Map<String, String> mapearLinha(CSVRecord line, List<String> colunas) {
    Map<String, String> linha = new LinkedHashMap<>();
    for (String coluna : colunas) {
      linha.put(coluna, obterValor(line, coluna));
    }

    return linha;
  }

  private String obterValor(CSVRecord line, String coluna) {
    if (coluna == null || coluna.isBlank()) {
      return null;
    }

    if (!line.isMapped(coluna)) {
      return null;
    }

    String valor = line.get(coluna);
    if (valor == null) {
      return null;
    }

    String normalizado = valor.trim();
    return normalizado.isBlank() ? null : normalizado;
  }
}