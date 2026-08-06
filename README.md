# Financial Assistant AI — Workshop Spring AI

Un asistente financiero inteligente construido con [Spring AI](https://docs.spring.io/spring-ai/reference) capaz de analizar transacciones bancarias, detectar patrones de gasto, identificar fraude y dar consejos financieros personalizados — todo en lenguaje natural.

---

## 🎯Objetivo

El proyecto simula el asistente financiero personal de un desarrollador que quiere entender en qué se gasta el dinero, detectar transacciones sospechosas y mejorar sus hábitos financieros.

A lo largo del workshop se construye iterativamente, empezando con un simple endpoint de chat y terminando con un sistema completo de detección de fraude y asesoramiento financiero.

---

## 🧠 Conceptos

| Concepto | Descripción |
|----------|-------------|
| **Modelos** | Abstracción de proveedor — mismo código para Ollama y OpenAI |
| **Prompt** | System prompt para definir el comportamiento del asistente |
| **Embeddings** | Conversión de transacciones en vectores semánticos |
| **Vector Store** | Búsqueda semántica sobre transacciones con [Chroma](https://www.trychroma.com) |
| **RAG** | Retrieval Augmented Generation sobre datos bancarios |
| **Tool Calling** | Consultas exactas a MongoDB desde el modelo |
| **Chat Memory** | Historial de conversación acumulado por sesión |
| **Structured Output** | Alertas de fraude como objetos Java tipados |

---

## 🛠️ Tecnologías

| Tecnología | Versión |
|-----------|---------|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Spring AI | 2.0.0-M5 |
| MongoDB | 7 |
| Chroma | Latest |
| Maven | 3.9+ |
| Docker Desktop | Latest |

---

## 🤖 Modelos de IA

| Rol | Proveedor | Modelo |
|-----|-----------|--------|
| Chat (local) | Ollama | `gemma4` |
| Chat (cloud) | OpenAI | `gpt-4.1-mini` |
| Embeddings | Ollama | `bge-m3` |

> `bge-m3` se usa siempre para embeddings independientemente del proveedor de chat, garantizando compatibilidad con Chroma al cambiar entre Ollama y OpenAI.

### Variantes de `gemma4` según tu hardware

El workshop se ha desarrollado en un **MacBook Pro M3 Pro — 11 cores, 14 GPU, 36GB RAM**.
Con ese hardware puedes correr los modelos más potentes de la familia:

| Modelo      | RAM necesaria | Recomendado para |
|-------------|------|-----------------|
| `gemma4` (e4b) | ~6GB | Equipos con 16GB RAM |
| `gemma4:26b` | ~20GB | MacBook Pro M3 Pro 36GB ✅ |
| `gemma4:31b` | ~24GB | MacBook Pro M3 Pro 36GB ✅ |

> 💡 Si tu equipo tiene menos de 16GB de RAM disponible, usa `gemma4:e6b`.
> Es más ligero y permite seguir el workshop sin problemas de rendimiento.
> Actualiza el modelo en `application-ollama.yml` según tu caso.

---

## ✅ Requisitos previos
- Java 21
- Maven 3.9+
- Docker Desktop
- [Ollama](https://ollama.com)
- Modelos y embbedings:
    ```bash
    # `gemma4` (default)
    $ ollama pull gemma4

    # `gemma4:26b`
    $ ollama pull gemma4:26b
    $ ollama pull bge-m3
    ```
- API key de OpenAI (Opcional)

---

## 🚀 Arrancar el proyecto

### Ollama (por defecto)
Sin coste, requiere Ollama corriendo)
```bash
$ mvn spring-boot:run
```

### OpenAI
Crear el fichero `.env.yml` y añadir el valor apikey, al arrancar la aplicacion se añadira
```bash
$ mvn spring-boot:run -P openai
```
---

## 🏗️Gestión de la infraestructura

```bash

# Arrancar contenedores
$ docker-compose up --build -d

# Parar contenedores (los datos persisten)
$ docker compose stop

# Parar y eliminar datos — reset completo
$ docker compose down -v
```

## 🧪 cURLs de demo por punto del workshop

---

### Hello Spring AI

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hola! Me llamo Diego, ¿cual es tu nombre?"}'
```
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Qué es Spring AI en una frase?"}'
```

### Con y sin prompt del sistema

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Qué te parece un pago de 847€ un sábado a las 3am?"}'
```

### RAG - Preguntas semánticas:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿A que supermercados suelo ir?"}'
```
✅Respuesta: "Los supermercados que visitas son Carrefour, Mercadona y Alcampo."

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿A que hora suelo comprar cafe?"}'
```
✅Respuesta: "Compras café en la mañana (alrededor de las 08:00 a 09:30) y también por la tarde (entre las 15:00 y 16:00)."

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Cuantas subscripciones pago?"}'
```
✅Respuesta: "Pagaste por 4 tipos de suscripciones: Amazon Prime, Netflix, Spotify y DAZN."

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Que restaurante frecuento mas?"}'
```
❌Respuesta incorrecta: "El restaurante que más frecuentes es Fábrica Excepcional."
ℹ️_El restaurante que mas frecuento es "Bodega La Ardosa"_

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Cuanto me he gastado en Karting en marzo de 2026?"}'
```
❌Respuesta incorrecta: "Te gastaste -100.00€ en Karting durante marzo de 2026."
ℹ️_El importe sería 160€_

### Tool Calling:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Que restaurante frecuento mas?"}'
```
✅Respuesta: "El restaurante que más frecuentas es Bodega La Ardosa."
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '¿Cuales son los 5 restaurantes que menos frecuento?"}'
```
✅Respuesta: "StreetXO, La Taverna, Bar La Peña, Juancho's BBQ y Pastelería Mallorca son los restaurantes que menos has frecuentado (una vez cada uno)."

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Cuanto me he gastado en Karting en marzo de 2026?"}'
```
✅Respuesta: "Te has gastado un total de -€160.00 en Karting durante marzo de 2026."

### Chat Memory:

### Antes:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Cuanto me he gastado en Karting en marzo de 2026?"}'
```
✅Respuesta: "Según el registro, te gastaste -160.00€ en Karting durante marzo de 2026."

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Y el mes pasado?"}'
```
❌Respuesta: "Te has gastado un total de -100.00€ en Karting durante febrero del 2026."
ℹ️_El importe sería 180€_

### Después:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Cuanto me he gastado en Karting en marzo de 2026?"}'
```
✅Respuesta: "Según el registro, te gastaste -160.00€ en Karting durante marzo de 2026."

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "¿Y el mes pasado?"}'
```
✅Respuesta: "Tu gasto en Karting el mes pasado (febrero de 2026) fue de -180.00€."

### Fraudulent transaction detection:

⚠️ Para esta prueba el modelo de `gemma4:e4b` no fue capaz de reconocer los patrones de fraude. 
Se opto por usa `qwen3.5:4b` en `application-ollama.yml`. 
Tambien se probo con la configuracion de _openai_ y el modelo de `gpt-4.1` con éxito.

```bash
curl -X POST 'http://localhost:8080/api/chat' \
--header 'Content-Type: application/json' \
--data '{"message": "Busca mas cargos sospechosos en diciembre"}'
```
### Antes de mejorar el prompt:
```
Cargos sospechosos detectados en diciembre:

Retiradas en cajero automático (patrón inusual):
  * 4 retiros entre el 29/12 consecutivos
  * Importes totales: -360.00€ (-150.00€, -100.00€, -60.00€, -50.00€)
    
Transferencias bancarias sin contexto:
  * -70.00€ totales en 2 días (17/12 y 17/12)

Patrones de gasto recurrente:
  * Resto: -84.50€ (cervezaría L'Europe, múltiples compras de 2-4€)
  * Cartas: -53.50€ (varios retiros pequeños sin fecha clara)
  
Recomendación: Revisa las transferencias no explicadas y los múltiples retiros en cajero el 29/12. ¿Quieres que calcule el total de gastos por categoría en diciembre?
```
### Despues de mejorar el prompt:
```
**ALERTA DIRECTA**: Retirada en cajero automático el 2025-12-29T02:04:47 por -100,00€.
**PATRÓN DETECTADO**: Se han realizado 4 retiradas de 100€, 50€, 150€ y 60€ en un tiempo de solo 23 segundos (desde las 02:04 hasta las 02:05 del día 29 de diciembre).
**ACCIÓN RECOMENDADA**: Congelamos tu tarjeta por seguridad inmediata. Si esta es una operación tuya, revívala y descongela la tarjeta en tu app. Si no la reconoces, contacta con soporte bancario ya mismo para denunciarla.
```