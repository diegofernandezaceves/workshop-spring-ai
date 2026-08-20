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
| Spring Boot | 4.1.0 |
| Spring AI | 2.0.0 |
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

### Structured Output:

⚠️ Para esta prueba el `qwen3.5:4b` a veces no es capaz de transacciones fraudulentas ilícitas.
Para que funcionen bien hay que echarles una mano e indicar el tipo de concepto o descripción de la transacción.

ℹ️ Usando la configuración de _openai_ y el modelo de `gpt-4.1` es capaz de detectar los patrones de fraude sin necesidad de indicar el tipo de concepto.

### Sin indicar el tipo de concepto
```bash
curl -X POST 'http://localhost:8080/api/chat/analyze' \
--header 'Content-Type: application/json' \
--data '{"message": "¿Hay alguna transaccion fraudulenta o sospechosa en diciembre?"}'
```

### Indicando el tipo de concepto
```bash
curl -X POST 'http://localhost:8080/api/chat/analyze' \
--header 'Content-Type: application/json' \
--data '{"message": ¿Hay alguna retirada de cajero fraudulenta en diciembre?"}'
```

✅ Respuesta:
```json
{
    "fraudDetected": true,
    "riskLevel": "HIGH",
    "reason": "Se han realizado 4 retiradas consecutivas en cajero automático de 160€ en un intervalo de solo 23 segundos, lo cual es físicamente imposible.",
    "recommendation": "Congelada la tarjeta y contacta con el banco inmediatamente para reportar las operaciones sospechosas. Verifica si estas transacciones son auténticas revisando los recibos.",
    "transactionIds": [
        "84e302ed-4ee6-4ea7-92c7-f33d5b4e7c82",
        "30b05401-ddba-4dbd-9523-3e4a48d319e9",
        "73635cfa-c1d8-47cb-8a7f-ddc642371aff",
        "6d3c1028-bedf-4adf-b475-ed9897876ff1"
    ]
} 
```

### Financial Advisor:

### Analizar gastos hormiga
```bash
curl -X POST 'http://localhost:8080/api/chat/advisor' \
--header 'Content-Type: application/json' \
--data '{"message": "¿Que gastos pude ahorrarme en el mes de marzo?"}'
```

ℹ️ ***Agregado para revisar los gastos totales por categoria***
```json
[
  {
    $match: 
    {
      datetime: {
        $gte: ISODate("2026-03-01T00:00:00Z"),
        $lte: ISODate("2026-03-31T23:59:59Z")
      }
    }
  },
  {
    $group: {
      _id: "$category",
      count: {
        $sum: "$amount"
      }
    }
  }
]
```

### Ollama - `qwen3.5:4b`
```txt
Identifico tu patrón concreto: en **marzo gastaste 594.50€ exclusivamente en la categoría restaurante**. Analizando tus transacciones, ese dinero se dividió entre cafés y cenas rápidas de diferentes cadenas (Café Gijón, Café Central, Manila, etc.), con compras puntuales más altas como un café por 5.00€ y otras por 4.00€.

Esto es un **clásico ejemplo de gasto hormiga** que el artículo destaca: *\"gastar 2 euros al día en café representa 60 euros al mes\"*. Tus cafés promedio oscilan entre 1.50€ y 3.00€ diarios, y suman más de la mitad del gasto total mensual en restaurante. El impacto va más allá del \"coco\": **esos desembolsos recurrentes impiden que alcances objetivos financieros importantes** como un fondo de emergencia o estabilidad financiera.

**Cuantificación del impacto:**
Si reduces los gastos en restaurante un **20%** (por ejemplo, evitando un café diario por 1.50€-2.00€, eligiendo agua y comida en casa), ahorrarías **118.90€ al mes**, lo que asciende a **1426.80€/año**. Si eliminas incluso el 50% de esos gastos hormiga, te quedarías con casi 300€ mensuales disponibles para reasignar.

**Acción inmediata:**
Cancela o limita por escrito tus suscripciones y visitas a cafeterías en marzo. Configura una alerta bancaria cuando cualquier gasto \"restaurante\" supere 2.50€ y verifica si se trata de un capricho real o un hábito que no valdrá la pena mantener.
```

### OpenAI - `GPT-4.1-mini`
```txt
En marzo registraste -594.50€ en restaurantes, -204.40€ en transporte y -90.00€ en retiradas en cajero. Estos son ejemplos claros de \"gastos hormiga\": pequeñas compras frecuentes (cafés, comidas fuera, recargas y efectivo) que se acumulan y reducen tu capacidad de ahorro.

Si reduces restaurante un 50%, ahorrarías 3567.00€/año.
Si reduces transporte un 30%, ahorrarías 735.84€/año.
Si reduces retiradas en cajero un 50%, ahorrarías 540.00€/año.

Regla única (acción para hoy): prepara tu café/snack y lleva agua de casa; pon el dinero equivalente de cada compra fuera en una cuenta de ahorros automática.
```

### Recomendacion de inversión del dinero ahorrado

```bash
curl -X POST 'http://localhost:8080/api/chat/advisor' \
--header 'Content-Type: application/json' \
--data '{"message": "¿Donde podria invertir el dinero ahorrado?"}'
```

### Ollama - `qwen3.5:4b`
```txt
**SITUACIÓN:** Tienes **2500,00€** ya reservados en tu cuenta de ahorros desde septiembre de 2025. Con un sueldo mensual de **2500,00€**, estás en una posición sólida para empezar a invertir hoy mismo. Tus gastos principales son: **hipoteca (800,00€/mes)**, **ocio Karting (aprox. 120,00€/mes)**, y **restaurante (unos 4,50€-3,50€ por día en los pocos días registrados)**.
**ESTRATEGIA:** Basándome en el artículo de inversión de 2026, la herramienta más respaldada para tu perfil es un **Fondo Indexado**. Las plataformas como **Indexa Capital o inbestMe** gestionan automáticamente una cartera diversificada. Cobran entre **0,15% y 0,50%** pero eliminan todas las decisiones de gestión. La estrategia que aplicas debe ser el **Dollar Cost Averaging (DCA)**: invertir una cantidad fija cada mes independientemente del mercado.
**PROYECCIÓN:** Si inviertes **250,00€ al año** (es decir, **20,83€ mensuales**), según el concepto de interés compuesto del artículo, en 10 años podrías alcanzar **396,00€**. Ese pequeño aporte inicial con el poder del tiempo es suficiente para proteger tu futuro. El artículo explica que invertir tardar es \"el único error imperdonable\" y que los fondos indexados tienen rentabilidades históricas sólidas con comisiones bajas.
**ACIÓN:** Abre **Indexa Capital o inbestMe** en tu app bancaria hoy mismo, realiza un test de perfil de inversor gratuito y configura la primera transferencia automática de **20,83€ mensuales** desde tu cuenta corriente a la plataforma. Esto aplica el DCA desde el primer mes: el mercado fluctúa, pero con regularidad ganas tiempo para invertir.
```

### OpenAI - `GPT-4.1-mini`
```txt
SITUACIÓN — Liquidez y capacidad de ahorro
- Saldo en tu cuenta a 31/03/2026: 2.660,84€.
- Evolución en marzo: empezaste el mes con 1.796,72€ y cerraste con 2.660,84€ → ahorraste 864,12€ en marzo.  
- Gasto fijo relevante: cuota de hipoteca mensual de -800,00€.

ESTRATEGIA (recomendada según el artículo)
- Opción principal para España: fondos indexados vía roboadvisor (p. ej. Indexa Capital). Ventajas: fiscalidad favorable en traspasos entre fondos, diversificación automática y gestión pasiva cómoda.  
- Alternativa si quieres control total: cartera propia con ETFs (más trabajo y consideraciones fiscales).  
- Reglas prácticas: define tu asset allocation (regla orientativa: 120 - edad = % en renta variable), usa DCA (aportación fija mensual) y rebalancea 1–2 veces al año.

PROYECCIÓN (ejemplo numérico)
- Si inviertes 864,12€/mes, según el concepto de fondos indexados (roboadvisor), podrías acelerar tu objetivo.  
- Ejemplo simplificado: invirtiendo 864,12€/mes durante 20 años y asumiendo un 5% anual compuesto, podrías acumular aproximadamente 355.049,60€.

ACCIÓN — Primer paso exacto que debes dar hoy
- Abre una cuenta en un roboadvisor que trabaje con fondos indexados (por ejemplo, Indexa Capital) y programa una aportación automática mensual de 864,12€ desde tu cuenta bancaria.

¿Quieres que abra el proceso con opciones concretas (Indexa vs MyInvestor) y te guíe paso a paso para crear la transferencia automática?
```
