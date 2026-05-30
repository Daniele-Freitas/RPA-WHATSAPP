package com.rpa.whatsapp.service;

import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpa.whatsapp.config.RabbitMQConfig;
import com.rpa.whatsapp.domain.Contato;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitMQSender {

  private final RabbitTemplate rabbitTemplate;

  private final ObjectMapper objectMapper;

  private final MensagemTemplateService mensagemTemplateService;

  public void publishContatos(List<Contato> contatos) {
    for (Contato contato : contatos) {
      String template = contato.getCampanha().getMensagemTemplate();
      String mensagem = mensagemTemplateService.render(template, contato.getVariaveis());
      ContatoMessage payload = new ContatoMessage(
          contato.getId(),
          contato.getCampanha().getId(),
          contato.getTelefone(),
          mensagem);
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

  private record ContatoMessage(UUID contatoId, UUID campanhaId, String telefone, String mensagem) {}
}
