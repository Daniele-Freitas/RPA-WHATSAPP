package com.rpa.whatsapp.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MensagemTemplateService {

  public String render(String template, Map<String, String> variaveis) {
    if (template == null) {
      return null;
    }

    String mensagem = template;
    if (variaveis == null || variaveis.isEmpty()) {
      return mensagem;
    }

    for (Map.Entry<String, String> entry : variaveis.entrySet()) {
      String chave = entry.getKey();
      String valor = entry.getValue();
      if (chave == null || chave.isBlank() || valor == null) {
        continue;
      }

      mensagem = mensagem.replace("{{" + chave + "}}", valor);
    }

    return mensagem;
  }
}
