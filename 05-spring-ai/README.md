# 🎙️ Budgeting AI — API de Orçamento por Voz com Spring Boot + Spring AI

> Entrega do **Desafio de Projeto DIO** — trilha *Itaú Java com Inteligência Artificial*.
> Fork do projeto do expert com melhorias próprias (validação, novo Tool Calling, correção de bug e testes).

## 📌 O que o projeto faz

API REST que recebe um **comando de voz** sobre gastos financeiros e responde **em áudio**:

```
🎤 áudio ──► Whisper (speech-to-text) ──► ChatClient + Tool Calling ──► Use Case ──► MySQL
                                                     │
🔊 mp3  ◄── TTS (text-to-speech) ◄── resposta da IA ◄┘
```

Exemplos de comando: *"Gastei 35 reais no mercado"*, *"Quanto eu gastei na farmácia?"*, *"Quanto gastei no total?"*

## ✨ Melhorias implementadas

| # | Melhoria | Onde |
|---|---|---|
| 1 | **Validação de domínio** antes de salvar: descrição obrigatória (máx. 255), valor > 0, categoria obrigatória | `domain/Transaction.java`, `domain/InvalidTransactionException.java` |
| 2 | **Erro 400 padronizado** (RFC 9457 *Problem Details*) em vez de 500 quando a regra é violada | `infrastructure/http/ApiExceptionHandler.java` |
| 3 | **Nova ferramenta de Tool Calling** `summarize-spending`: total gasto e nº de transações por categoria + total geral | `application/SummarizeSpendingUseCase.java` |
| 4 | **Novo endpoint REST** `GET /transactions/summary` reaproveitando o mesmo use case da IA | `TransactionController.java` |
| 5 | **Correção de bug**: o valor era salvo em centavos mas devolvido como se fosse reais (1050 → 1050.0). Agora 1050 → **10.50** | `application/output/TransactionOutput.java` |
| 6 | **Prompt de sistema melhorado**: regra de centavos, uso da ferramenta de resumo, tratamento de erro de validação e respostas curtas (viram áudio) | `resources/prompts/system-message.st` |
| 7 | **15 testes unitários** que rodam **sem API key e sem banco** (repositório fake em memória) | `src/test/java/dio/budgeting/{domain,application,support}` |

Como a IA chama os mesmos use cases do REST, a validação vale pros dois caminhos: se o modelo tentar salvar um valor inválido, o Spring AI devolve o erro pra IA, que explica o problema pro usuário por voz.

## 🛠️ Tecnologias

- Java 25 · Spring Boot 4 · Spring AI 2.0 (OpenAI)
- `gpt-4o-mini` (chat + tool calling) · `whisper-1` (transcrição) · `gpt-4o-mini-tts` (voz)
- Spring Data JPA · MySQL 9 · Docker Compose
- Lombok · JUnit 5 · AssertJ · Gradle

## ▶️ Como executar

Pré-requisitos: **JDK 25**, **Docker** e uma **chave da OpenAI**.

```bash
export OPENAI_API_KEY="sua_chave"
./gradlew bootRun        # o Spring sobe o MySQL do compose.yml automaticamente
```

Só os testes unitários (não precisam de chave nem Docker):

```bash
./gradlew test --tests 'dio.budgeting.domain.*' --tests 'dio.budgeting.application.*' --tests 'dio.budgeting.ApiExceptionHandlerTest'
```

> Os testes `*IT` só rodam quando `OPENAI_API_KEY` está definida.

## 🧪 Como testar o fluxo principal

**1. Comando de voz** (tem áudios de exemplo em `src/test/resources/audio`):

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@src/test/resources/audio/recording-1.m4a" \
  --output resposta.mp3
```

**2. Criar transação via REST** (`amount` em centavos):

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Mercado","category":"GROCERIES","amount":3550}'
```

**3. Validação em ação** → `400 Bad Request`:

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"","category":"GROCERIES","amount":0}'
```

```json
{ "title": "Transação inválida", "status": 400, "detail": "A descrição da transação é obrigatória." }
```

**4. Listar por categoria e resumo:**

```bash
curl http://localhost:8080/transactions/GROCERIES
curl http://localhost:8080/transactions/summary
```

```json
{
  "categories": [
    { "category": "GROCERIES", "transactions": 2, "total": 30.5 },
    { "category": "PHARMA",    "transactions": 1, "total": 15.99 },
    { "category": "AUTO",      "transactions": 0, "total": 0.0 }
  ],
  "total": 46.49
}
```

Categorias: `GROCERIES` (mercado), `PHARMA` (farmácia), `AUTO` (carro).

## 📚 O que aprendi

- **Tool Calling** é o que liga a IA ao sistema real: o modelo não acessa o banco, ele só escolhe qual `@Tool` chamar — a regra de negócio continua no código.
- Colocar a **validação no domínio** protege os dois caminhos (REST e IA) de uma vez, sem duplicar código.
- Separar **domínio / aplicação / infraestrutura** deixa os use cases testáveis com um repositório fake, sem subir Spring, banco ou pagar API.
- O **prompt de sistema** importa: deixar explícito que o valor é em centavos e que a resposta vai virar áudio melhora muito o resultado.
- Lendo o código achei um bug de conversão centavos → reais, que só apareceu ao escrever os testes.

---

<details>
<summary>📄 README original do projeto (expert)</summary>

# DIO Spring Boot - Final Project 05: Spring AI (budgeting)

## Introduction

This final module applies Spring AI in a budgeting API while preserving the same layered architecture used across the track.

The goal is to integrate AI capabilities without bypassing domain and use case boundaries.

## Code Context

The project processes voice commands to create and query financial transactions.

Primary flow:

1. Client uploads an audio file.
2. Audio is transcribed into text.
3. The model selects an application tool/use case.
4. The use case persists or queries transaction data.
5. The final response is converted to audio.

## Project Structure

- `src/main/java/dio/budgeting/domain`
  - Domain model and repository contract.
- `src/main/java/dio/budgeting/application`
  - Use cases used by both REST and AI tool calling.
- `src/main/java/dio/budgeting/infrastructure`
  - HTTP adapters, JPA adapters, and integration glue.

## Module-Specific Topics

### Speech-to-text

- Uses `TranscriptionModel` for audio transcription.
- Model settings are configured in `application.properties`.

### Tool calling

- `ChatClient` registers use-case tools.
- `@Tool` methods expose business capabilities to the model.

### Text-to-speech

- `TextToSpeechModel` produces MP3 output from final text.
- AI endpoint returns generated audio.

## Spring AI Documentation

- Spring AI Reference: https://docs.spring.io/spring-ai/reference/index.html
- ChatModel API: https://docs.spring.io/spring-ai/reference/api/chatmodel.html
- ChatClient API: https://docs.spring.io/spring-ai/reference/api/chatclient.html
- Tools API: https://docs.spring.io/spring-ai/reference/api/tools.html
- Audio Transcriptions API: https://docs.spring.io/spring-ai/reference/api/audio/transcriptions.html
- Audio Speech API: https://docs.spring.io/spring-ai/reference/api/audio/speech.html

## Shared Architecture References

Common architecture concepts are documented in the root README:

- [DDD layers](../README.md#ddd-layered-architecture)
- [Class vs record](../README.md#java-class-vs-java-record-in-domain-modeling)
- [Strong typed identifiers](../README.md#strong-typed-identifiers)
- [Repository pattern](../README.md#repository-pattern)
- [Use cases and Clean Architecture](../README.md#use-cases-and-clean-architecture)
- [Docker Compose support](../README.md#docker-compose-support-in-development)

## How to Run

Set your OpenAI API key:

```bash
export OPENAI_API_KEY="your_api_key_here"
```

Run the application and tests:

```bash
./gradlew bootRun
./gradlew test
```

## Notes

- Educational final project focused on AI plus architectural discipline.
- External provider integration tests may require active credentials.


</details>
