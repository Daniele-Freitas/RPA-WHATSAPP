package com.rpa.whatsapp.service;

import org.springframework.stereotype.Service;

@Service
public class TelefoneService {

  public String sanitizar(String telefoneBruto) {
    if (telefoneBruto == null) {
      return null;
    }

    String limpo = telefoneBruto.replaceAll("\\D", "");
    if (limpo.length() == 10 || limpo.length() == 11) {
      limpo = "55" + limpo;
    }

    if (limpo.length() < 12 || limpo.length() > 13) {
      return null;
    }

    return limpo;
  }

  public boolean isValido(String telefoneBruto) {
    return sanitizar(telefoneBruto) != null;
  }
}
