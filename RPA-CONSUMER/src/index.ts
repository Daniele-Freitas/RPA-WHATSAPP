import { startConsumer } from "./rabbitmq.js";
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
  let status: StatusEnvio = "SUCESSO";

  try {
    await client.sendMessage(payload.telefone, payload.mensagem);
    console.log(`Mensagem enviada: contato ${payload.contatoId}`);
  } catch (error) {
    status = "ERRO";

    if (error instanceof NumeroNaoExisteError) {
      console.warn(`Número não existe: ${payload.telefone}`);
    } else if (error instanceof TimeoutCampoMensagemError) {
      console.warn(`Timeout aguardando campo de mensagem: ${payload.telefone}`);
    } else if (error instanceof ErroEnvioMensagemError) {
      console.error(`Falha ao enviar: ${error.message}`);
    } else {
      const message = error instanceof Error ? error.message : String(error);
      console.error(`Erro inesperado: ${message}`);
    }
  }

  try {
    await atualizarStatusContato(BACKEND_BASE_URL, payload.contatoId, status);
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
