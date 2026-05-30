import { chromium, Page } from "playwright";

export class NumeroNaoExisteError extends Error {
  constructor(numero: string) {
    super(`Número ${numero} não está registrado no WhatsApp`);
    this.name = "NumeroNaoExisteError";
  }
}

export class TimeoutCampoMensagemError extends Error {
  constructor(numero: string) {
    super(`Timeout esperando campo de mensagem para ${numero}`);
    this.name = "TimeoutCampoMensagemError";
  }
}

export class ErroEnvioMensagemError extends Error {
  constructor(numero: string, motivo: string) {
    super(`Falha ao enviar mensagem para ${numero}: ${motivo}`);
    this.name = "ErroEnvioMensagemError";
  }
}

interface WhatsAppClient {
  sendMessage: (telefone: string, mensagem: string) => Promise<void>;
  close: () => Promise<void>;
}

export async function createWhatsAppClient(sessionDir: string): Promise<WhatsAppClient> {
  const context = await chromium.launchPersistentContext(sessionDir, {
    headless: false,
    viewport: null
  });

  const page = await context.newPage();

  console.log("Abrindo WhatsApp Web. Faça login se necessário...");
  await page.goto("https://web.whatsapp.com/", { waitUntil: "domcontentloaded" });
  await page.waitForSelector("#pane-side", { timeout: 60000 });
  console.log("Login confirmado. Consumer pronto.");

  return {
    sendMessage: (telefone, mensagem) => enviarMensagem(page, telefone, mensagem),
    close: () => context.close()
  };
}

async function enviarMensagem(page: Page, telefone: string, mensagem: string): Promise<void> {
  if (!telefone || !mensagem) {
    throw new Error("Telefone e mensagem são obrigatórios");
  }

  const url = `https://web.whatsapp.com/send?phone=${telefone}&text=${encodeURIComponent(mensagem)}`;
  await page.goto(url, { waitUntil: "domcontentloaded" });

  const temErro = await verificarErroNumeroNaoExiste(page);
  if (temErro) {
    throw new NumeroNaoExisteError(telefone);
  }

  try {
    await page.waitForSelector('[contenteditable="true"]', { timeout: 15000 });
  } catch {
    throw new TimeoutCampoMensagemError(telefone);
  }

  try {
    await clicarEnviar(page);
  } catch (error) {
    const motivo = error instanceof Error ? error.message : "Erro desconhecido";
    throw new ErroEnvioMensagemError(telefone, motivo);
  }
}

async function verificarErroNumeroNaoExiste(page: Page): Promise<boolean> {
  try {
    await page.waitForTimeout(1500);
    const modalErro = await page.$('div[role="alertdialog"]');

    if (!modalErro) {
      return false;
    }

    const texto = await page.evaluate(() => {
      const modal = document.querySelector('div[role="alertdialog"]');
      return modal?.textContent?.toLowerCase() || "";
    });

    if (
      texto.includes("não") ||
      texto.includes("válid") ||
      texto.includes("n/a") ||
      texto.includes("appears to be invalid") ||
      texto.includes("doesn't exist")
    ) {
      const botaoOK = await page.$('button:has-text("OK"), button:has-text("Ok"), div[role="button"]:has-text("OK")');
      if (botaoOK) {
        await botaoOK.click();
      } else {
        await page.evaluate(() => {
          const btn = Array.from(document.querySelectorAll('button, div[role="button"]'))
            .find(el => el.textContent?.trim() === "OK" || el.textContent?.trim() === "Ok");
          if (btn && btn instanceof HTMLElement) {
            btn.click();
          }
        });
      }

      await page.waitForTimeout(500);
      return true;
    }

    return false;
  } catch {
    return false;
  }
}

async function clicarEnviar(page: Page): Promise<void> {
  let enviado = false;

  try {
    await page.keyboard.press("Enter");
    await page.waitForTimeout(500);
    enviado = true;
  } catch {
    // fallback
  }

  if (!enviado) {
    try {
      await page.click('button[aria-label*="send"], button[aria-label*="Send"]', { timeout: 5000 });
      await page.waitForTimeout(500);
      enviado = true;
    } catch {
      // fallback
    }
  }

  if (!enviado) {
    await page.evaluate(() => {
      const btn = document.querySelector('button[aria-label*="send"], button[aria-label*="Send"]') as HTMLButtonElement;
      if (btn) {
        btn.click();
      }
    });
    await page.waitForTimeout(500);
  }
}
