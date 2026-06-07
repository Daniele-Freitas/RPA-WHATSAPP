import amqplib, { ConsumeMessage } from "amqplib";
import { ContatoMessage } from "./types.js";

interface ConsumerConfig {
  url: string;
  queue: string;
  onMessage: (payload: ContatoMessage) => Promise<void>;
}

export async function startConsumer(config: ConsumerConfig): Promise<void> {
  const connection = await amqplib.connect(config.url);
  const channel = await connection.createChannel();

  await channel.assertQueue(config.queue, { durable: true });
  channel.prefetch(1);

  channel.consume(config.queue, async (msg: ConsumeMessage | null) => {
    if (!msg) {
      return;
    }

    try {
      const payload = parsePayload(msg.content.toString());
      await config.onMessage(payload);
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      console.error(`Erro ao processar mensagem: ${message}`);
    } finally {
      channel.ack(msg);
    }
  });

  console.log(`Consumer conectado na fila ${config.queue}`);
}

function parsePayload(raw: string): ContatoMessage {
  const data = JSON.parse(raw) as Partial<ContatoMessage>;

  // Validação atualizada para aceitar o array "telefones"
  if (!data.contatoId || !data.telefones || !Array.isArray(data.telefones) || data.telefones.length === 0 || !data.mensagem) {
    throw new Error("Payload inválido: contatoId, telefones (array não vazio) e mensagem são obrigatórios");
  }

  return {
    contatoId: data.contatoId,
    campanhaId: data.campanhaId ?? "",
    telefones: data.telefones, // Recebe a lista priorizada do Spring Boot
    mensagem: data.mensagem
  };
}