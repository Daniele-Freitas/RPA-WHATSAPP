package com.rpa.whatsapp.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileConverterService {

    // Chave gratuita do OCR.space (Você pode pegar uma no site deles dps)
    private static final String OCR_API_KEY = "helloworld"; 
    private static final String OCR_API_URL = "https://api.ocr.space/parse/image";

    /**
     * O Método Principal: Recebe qualquer arquivo e devolve um CSV em formato String
     */

    public String converterParaCsv(MultipartFile arquivo) throws IOException, IllegalArgumentException {
        String nomeArquivo = arquivo.getOriginalFilename();

        if(nomeArquivo == null) {
            throw new IllegalArgumentException("O arquivo deve ter um nome válido.");
        }else{
            if(nomeArquivo.endsWith(".csv")){
                // Se já for CSV, apenas lê e retorna o conteúdo
                return new String(arquivo.getBytes(), StandardCharsets.UTF_8);
            }
            else if(nomeArquivo.endsWith(".xlsx") || nomeArquivo.endsWith(".xls")){
                // Se for um arquivo Excel, converte para CSV
                return converterExcel(arquivo);
            }
            else if (nomeArquivo.matches(".*\\.(pdf|jpg|jpeg|png)$")) {
                // Se é Foto ou PDF, manda para a API de OCR
                return converterImagemOuPdf(arquivo);
            }
        }
            
        throw new IllegalArgumentException("Formato de arquivo não suportado.");
    }
    /**
     * Converte abas do Excel em texto CSV (Linhas separadas por vírgula)
    */

    private String converterExcel(MultipartFile arquivo) throws IOException, IllegalArgumentException {
        StringBuilder csvBuilder = new StringBuilder();

        try (InputStream is = arquivo.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)){

            Sheet sheet = workbook.getSheetAt(0); // Pega a primeira aba
            for (Row row : sheet){
                StringBuilder linhaCsv = new StringBuilder();
                for (Cell cell : row){
                    //pega o valor da celula como string(ignorando formatações)
                    DataFormatter formatter = new DataFormatter();
                    String valor = formatter.formatCellValue(cell).replace(",","");// Remove vírgulas para não quebrar o formato CSV
                    linhaCsv.append(valor).append(",");
                }
                // Remove a última vírgula da linha
                if (linhaCsv.isEmpty()) {
                    linhaCsv.setLength(linhaCsv.length() - 1);
                }
                csvBuilder.append(linhaCsv).append("\n");
            }
        }
        return csvBuilder.toString();
    }

    /**
    * Envia para o OCR.space e transforma o texto bagunçado em um CSV limpo     
    **/

    private String converterImagemOuPdf(MultipartFile arquivo) throws IOException, IllegalArgumentException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("apikey", OCR_API_KEY);

        // Prepara o arquivo para enviar na requisição HTTP
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(arquivo.getBytes()) {
            @Override
            public String getFilename() { return arquivo.getOriginalFilename(); }
        });

        body.add("language", "por"); // Define o idioma como Português
        body.add("isTable", "true"); // Pede para a API tentar ler em formato de tabela

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Dispara a requisição
        ResponseEntity<String> response = restTemplate.postForEntity(OCR_API_URL, requestEntity, String.class);

        // Extrai o texto da resposta JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String textoExtraido = root.path("ParsedResults").get(0).path("ParsedText").asText();

        // Transforma o texto livre em um CSV (Nome, Telefone)
        return estruturarTextoEmCsv(textoExtraido);
        }

        /**
         * Algoritmo de Inteligência: Garimpa telefones dentro de um texto qualquer
         */
        
        private String estruturarTextoEmCsv(String textoBruto) {
            StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append("NomeExtraido,TelefoneExtraido\n"); // Cria o cabeçalho padrão

            String[] linhas = textoBruto.split("\n");
            
            // Regex para caçar números de telefone (com ou sem DDD, com ou sem traço)
            Pattern phonePattern = Pattern.compile("\\+?\\d{2}?\\s?\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}");

            for (String linha : linhas) {
                Matcher matcher = phonePattern.matcher(linha);
                if (matcher.find()) {
                    String telefone = matcher.group().replaceAll("\\D", ""); // Pega o telefone e tira espaços/símbolos
                    
                    // O nome provável é o resto do texto da linha que não é número
                    String provavelNome = linha.replaceAll(matcher.group(), "").replaceAll("[^a-zA-ZÀ-ÿ\\s]", "").trim();
                    
                    if (provavelNome.isEmpty()) provavelNome = "Contato";

                    csvBuilder.append(provavelNome).append(",").append(telefone).append("\n");
                }
            }

            return csvBuilder.toString();
        }    
}
