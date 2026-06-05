package com.rpa.whatsapp.service;

import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpa.whatsapp.config.RabbitMQConfig;
import com.rpa.whatsapp.domain.Contato;
import com.rpa.whatsapp.domain.Telefone;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitMQSender {

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;
  private final MensagemTemplateService mensagemTemplateService;

  public void publishContatos(List<Contato> contatos) {
    if (contatos == null) return;

    for (Contato contato : contatos) {
      if (contato.getTelefones() == null || contato.getTelefones().isEmpty()) {
        continue; // Pula se não tiver nenhum telefone
      }

      String template = contato.getCampanha().getMensagemTemplate();
      String mensagem = mensagemTemplateService.render(template, contato.getVariaveis());

      // Extrai apenas as strings dos números, mantendo a ordem (o primeiro é o principal)
      List<String> numerosPriorizados = contato.getTelefones().stream()
          .map(Telefone::getNumero) 
          .toList();

      // Monta o payload enviando a lista completa para o Worker (TypeScript)
      ContatoMessage payload = new ContatoMessage(
          contato.getId(),
          contato.getCampanha().getId(),
          numerosPriorizados, // Agora enviamos o array inteiro
          mensagem
      );
      
      rabbitTemplate.convertAndSend(RabbitMQConfig.WHATSAPP_JOBS_QUEUE, toJson(payload));
    }
  }

  private String toJson(ContatoMessage payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to serialize contato message", ex);
    }
  }

  // O payload agora espera uma List<String> no lugar da String simples
  private record ContatoMessage(UUID contatoId, UUID campanhaId, List<String> telefones, String mensagem) {}
}