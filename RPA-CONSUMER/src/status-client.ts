import { StatusEnvio } from "./types.js";

export async function atualizarStatusContato(
  baseUrl: string,
  contatoId: string,
  statusEnvio: StatusEnvio
): Promise<void> {
  const response = await fetch(`${baseUrl}/api/contatos/${contatoId}/status`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ statusEnvio })
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`Falha ao atualizar status (${response.status}): ${body}`);
  }
}
