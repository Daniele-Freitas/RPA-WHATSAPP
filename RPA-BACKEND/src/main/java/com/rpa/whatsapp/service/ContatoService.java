package com.rpa.whatsapp.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.rpa.whatsapp.domain.Campanha;
import com.rpa.whatsapp.domain.Contato;
import com.rpa.whatsapp.domain.StatusEnvio;
import com.rpa.whatsapp.domain.Telefone;
import com.rpa.whatsapp.dto.ContatoRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContatoService {

  private final TelefoneService telefoneService;

  public List<String> listarTelefonesInvalidos(List<ContatoRequest> contatos) {
    List<String> invalidos = new ArrayList<>();
    if (contatos == null) {
      return invalidos;
    }

    for (ContatoRequest contato : contatos) {
      if (contato.getTelefones() != null) {
        for (String telefone : contato.getTelefones()) {
          if (!telefoneService.isValido(telefone)) {
            invalidos.add(telefone == null ? "" : telefone);
          }
        }
      }
    }
    return invalidos;
  }

  public Contato criarContato(ContatoRequest request, Campanha campanha){    
    if (request == null || request.getTelefones() == null || request.getTelefones().isEmpty()) {
      return null;
    }

    String nome = normalizarNome(request.getNome());
    Map<String, String> variaveis = montarVariaveis(request.getVariaveis(), nome);

    // 1. Criamos a entidade Pai (Contato) primeiro, SEM os telefones
    Contato contato = Contato.builder()
        .nome(nome)
        .variaveis(new HashMap<>(variaveis))
        .statusEnvio(StatusEnvio.PENDENTE)
        .campanha(campanha)
        .build();

    // 2. Preparamos a lista que vai guardar as entidades Filhas (Telefone)
    List<Telefone> entidadesTelefone = new ArrayList<>();

    // 3. Fazemos o loop na lista de strings que veio do JSON
    for (String telefoneStr : request.getTelefones()) {
      if (telefoneService.isValido(telefoneStr)) {
        String telefoneSanitizado = telefoneService.sanitizar(telefoneStr);

        // Criamos a entidade filha e já vinculamos o pai nela (Relacionamento Bidirecional)
        Telefone telefoneEntidade = Telefone.builder()
            .numero(telefoneSanitizado) 
            .contato(contato) 
            .build();

        entidadesTelefone.add(telefoneEntidade);
      }
    }

    // 4. Se nenhum telefone for válido, evitamos salvar um contato fantasma
    if (entidadesTelefone.isEmpty()) {
        return null; 
    }

    // 5. Entregamos a lista de filhos para o pai
    contato.setTelefones(entidadesTelefone);

    return contato;
  }

  private Map<String, String> montarVariaveis(Map<String, String> variaveisRequest, String nome) {
    Map<String, String> variaveis = new HashMap<>();

    if (variaveisRequest != null) {
      for (Map.Entry<String, String> entry : variaveisRequest.entrySet()) {
        if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
          continue;
        }
        variaveis.put(entry.getKey(), entry.getValue());
      }
    }

    if (nome != null) {
      variaveis.putIfAbsent("nome", nome);
      String primeiroNome = extrairPrimeiroNome(nome);
      if (primeiroNome != null) {
        variaveis.putIfAbsent("primeiro_nome", primeiroNome);
      }
    }

    variaveis.putIfAbsent("saudacao", obterSaudacao(LocalTime.now()));

    return variaveis;
  }

  private String normalizarNome(String nome) {
    if (nome == null) {
      return null;
    }

    String normalizado = nome.trim().replaceAll("\\s+", " ");
    return normalizado.isBlank() ? null : normalizado;
  }

  private String extrairPrimeiroNome(String nome) {
    if (nome == null) {
      return null;
    }

    String[] partes = nome.split("\\s+");
    return partes.length == 0 ? null : partes[0];
  }

  private String obterSaudacao(LocalTime horario) {
    int hora = horario.getHour();
    if (hora >= 5 && hora < 12) {
      return "Bom dia";
    }
    if (hora >= 12 && hora < 18) {
      return "Boa tarde";
    }
    return "Boa noite";
  }
}