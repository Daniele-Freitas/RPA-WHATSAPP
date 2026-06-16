import { startConsumer } from "./rabbitmq";
import { atualizarStatusContato } from "./status-client.js";
import {
  createWhatsAppClient,
  ErroEnvioMensagemError,
  NumeroNaoExisteError,
  TimeoutCampoMensagemError
} from "./whatsapp.js";
import { ContatoMessage, StatusEnvio } from "./types.js";

const RABBITMQ_URL = process.env.RABBITMQ_URL ?? "amqp://guest:guest@localhost:5672";
const RABBITMQ_QUEUE = process.env.RABBITMQ_QUEUE ?? "whatsapp_jobs";
const BACKEND_BASE_URL = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
const WHATSAPP_SESSION_DIR = process.env.WHATSAPP_SESSION_DIR ?? "./sessao_wpp";
const SEND_DELAY_MS = Number(process.env.SEND_DELAY_MS ?? "0");

function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function processarMensagem(
  client: Awaited<ReturnType<typeof createWhatsAppClient>>,
  payload: ContatoMessage
): Promise<void> {
  // Começamos assumindo erro. Só muda para sucesso se um dos envios funcionar.
  let statusFinal: StatusEnvio = "ERRO";

  console.log(`\nIniciando job para contato ${payload.contatoId} (Possui ${payload.telefones.length} número(s))`);

  // O Loop de Fallback
  for (let i = 0; i < payload.telefones.length; i++) {
    const telefoneAtual = payload.telefones[i];

    if(!telefoneAtual) continue; // Pula entradas vazias ou nulas;
    
    console.log(`[Tentativa ${i + 1}/${payload.telefones.length}] Testando número: ${telefoneAtual}`);

    try {
      await client.sendMessage(telefoneAtual, payload.mensagem);
      console.log(`✅ Sucesso! Mensagem enviada para ${telefoneAtual}`);
      statusFinal = "SUCESSO";
      break; // Interrompe o loop! Não precisamos tentar os próximos números.
      
    } catch (error) {
      if (error instanceof NumeroNaoExisteError) {
        console.warn(`⚠️ Número não existe no WhatsApp: ${telefoneAtual}`);
      } else if (error instanceof TimeoutCampoMensagemError) {
        console.warn(`⏳ Timeout aguardando campo de mensagem: ${telefoneAtual}`);
      } else if (error instanceof ErroEnvioMensagemError) {
        console.error(`❌ Falha ao enviar: ${error.message}`);
      } else {
        const message = error instanceof Error ? error.message : String(error);
        console.error(`💥 Erro inesperado: ${message}`);
      }
      
      // Se tiver mais números no array, o loop recomeça para o próximo índice.
      if (i < payload.telefones.length - 1) {
        console.log("➡️ Acionando fallback para o próximo número da lista...");
      } else {
        console.log("🚫 Todos os números falharam.");
      }
    }
  }

  // Atualiza o Spring Boot com o veredito final
  try {
    await atualizarStatusContato(BACKEND_BASE_URL, payload.contatoId, statusFinal);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.error(`Falha ao atualizar status no backend: ${message}`);
  }

  if (SEND_DELAY_MS > 0) {
    await sleep(SEND_DELAY_MS);
  }
}

async function main(): Promise<void> {
  const whatsappClient = await createWhatsAppClient(WHATSAPP_SESSION_DIR);

  await startConsumer({
    url: RABBITMQ_URL,
    queue: RABBITMQ_QUEUE,
    onMessage: (payload) => processarMensagem(whatsappClient, payload)
  });
}

try {
  await main();
} catch (error) {
  const message = error instanceof Error ? error.message : String(error);
  console.error(`Falha ao iniciar consumer: ${message}`);
  process.exit(1);
}