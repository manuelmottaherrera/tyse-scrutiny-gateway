# Presupuesto: Extracción OCR de Formularios E14

**Fecha:** 2026-02-23
**Proyecto:** Tyse Scrutiny
**Autor:** Manuel Motta / Claude Code

---

## 1. Resumen Ejecutivo

Este documento analiza los costos de extracción de texto mediante OCR de los formularios E14 electorales utilizando servicios cloud de inteligencia artificial documental.

### Contexto del Proyecto

| Parámetro        | Valor                                                    |
| ---------------- | -------------------------------------------------------- |
| Volumen estimado | ~100,000 formularios E14 por escrutinio                  |
| Páginas por E14  | Variable (2-8 páginas según tipo de elección)            |
| Procesamiento    | Paralelo (tiempo crítico durante escrutinio)             |
| Flujo            | `E14 (PDF) → MinIO → Servicio OCR → Datos estructurados` |

### Recomendación Rápida

**AWS Textract** ofrece la mejor relación costo-beneficio para este caso de uso:

- Costo más bajo por página para extracción de formularios
- Límites de concurrencia más altos (100+ jobs paralelos)
- Sin costo adicional por procesamiento asíncrono

---

## 2. Comparativa de Precios por Página

### 2.1 Precios Básicos (OCR/Detección de Texto)

| Servicio                        |                 Primeras 1M páginas |    Después de 1M páginas |
| ------------------------------- | ----------------------------------: | -----------------------: |
| **AWS Textract**                |                      $0.0015/página |           $0.0006/página |
| **Azure Document Intelligence** | $1.50/1000 páginas ($0.0015/página) |                  Similar |
| **Google Document AI**          | $1.50/1000 páginas ($0.0015/página) | $0.60/1000 después de 5M |

### 2.2 Precios para Extracción de Formularios (Campos estructurados)

Para formularios E14, necesitamos extracción de **campos estructurados** (tablas + formularios), no solo OCR básico:

| Servicio                                   |          Tablas |     Formularios | Tablas + Formularios |
| ------------------------------------------ | --------------: | --------------: | -------------------: |
| **AWS Textract**                           |      $0.015/pág |       $0.05/pág |            $0.07/pág |
| **Azure Document Intelligence** (Prebuilt) | ~$0.01-0.02/pág | ~$0.01-0.02/pág |    ~$0.02-0.04/pág\* |
| **Google Document AI** (Custom)            | $10-30/1000 pág | $10-30/1000 pág |       $0.01-0.03/pág |

\*Los precios de Azure varían según región y acuerdo comercial.

### 2.3 Tier Gratuito

| Servicio                        | Límite Gratuito                                  | Duración   |
| ------------------------------- | ------------------------------------------------ | ---------- |
| **AWS Textract**                | 1,000 pág/mes (texto), 100 pág/mes (formularios) | 3 meses    |
| **Azure Document Intelligence** | 500 páginas/mes                                  | Permanente |
| **Google Document AI**          | $300 crédito inicial                             | Único      |

---

## 3. Escenarios de Costo para 100,000 E14

### Escenario A: 2 páginas promedio por E14 (200,000 páginas)

| Servicio                        | Costo OCR Básico | Costo Formularios |      Costo Total |
| ------------------------------- | ---------------: | ----------------: | ---------------: |
| **AWS Textract**                |             $300 |           $10,000 |      **$10,300** |
| **Azure Document Intelligence** |             $300 |     ~$4,000-8,000 | **$4,300-8,300** |
| **Google Document AI**          |             $300 |      $2,000-6,000 | **$2,300-6,300** |

### Escenario B: 4 páginas promedio por E14 (400,000 páginas)

| Servicio                        | Costo OCR Básico | Costo Formularios |       Costo Total |
| ------------------------------- | ---------------: | ----------------: | ----------------: |
| **AWS Textract**                |             $600 |           $20,000 |       **$20,600** |
| **Azure Document Intelligence** |             $600 |    ~$8,000-16,000 | **$8,600-16,600** |
| **Google Document AI**          |             $600 |     $4,000-12,000 | **$4,600-12,600** |

### Escenario C: 8 páginas promedio por E14 (800,000 páginas)

| Servicio                        | Costo OCR Básico | Costo Formularios |        Costo Total |
| ------------------------------- | ---------------: | ----------------: | -----------------: |
| **AWS Textract**                |           $1,200 |           $40,000 |        **$41,200** |
| **Azure Document Intelligence** |           $1,200 |   ~$16,000-32,000 | **$17,200-33,200** |
| **Google Document AI**          |           $1,200 |     $8,000-24,000 |  **$9,200-25,200** |

### Resumen Visual de Costos

```
Costo estimado para 100,000 E14 (extracción de formularios)

Páginas/E14:    2 págs          4 págs          8 págs
                ─────────       ─────────       ─────────
AWS Textract:   $10,300         $20,600         $41,200
Azure:          $4,300-8,300    $8,600-16,600   $17,200-33,200
Google:         $2,300-6,300    $4,600-12,600   $9,200-25,200
```

---

## 4. Procesamiento Paralelo y Límites de Concurrencia

### 4.1 AWS Textract

| Métrica                     | Límite Default | Máximo (con solicitud) |
| --------------------------- | -------------: | ---------------------: |
| TPS (Transacciones/segundo) |         15 TPS |          Incrementable |
| Jobs concurrentes (async)   |       100 jobs |              600+ jobs |
| Páginas por documento async |  3,000 páginas |                      - |

**Ventajas:**

- Sin costo adicional por procesamiento asíncrono
- Soporte nativo para PDFs multi-página
- Jobs concurrentes altos (100-600)

**Tiempo estimado para 100K E14 (2 págs c/u):**

- Con 100 jobs concurrentes: ~33 minutos
- Con 600 jobs concurrentes: ~6 minutos

### 4.2 Azure Document Intelligence

| Métrica                     | Límite Default | Máximo (con solicitud) |
| --------------------------- | -------------: | ---------------------: |
| TPS (Transacciones/segundo) |         15 TPS |          Incrementable |
| Requests concurrentes       | ~15 por región |          Incrementable |

**Consideraciones:**

- Límite de 15 TPS por región (900 requests/minuto)
- Se puede distribuir carga entre regiones
- Requiere solicitud de soporte para aumentar límites

**Tiempo estimado para 100K E14:**

- Con 15 TPS: ~111 minutos (~2 horas)
- Con aumento de cuota (60 TPS): ~28 minutos

### 4.3 Google Document AI

| Métrica             | Límite Default |
| ------------------- | -------------: |
| Requests/minuto     |            120 |
| Procesamiento batch |      Soportado |

**Consideraciones:**

- Procesamiento batch disponible
- Costo adicional por hosting de procesadores ($0.05/hora)

---

## 5. Análisis de Capacidad para Escrutinio

### Requisito: Procesar 100,000 E14 en tiempo crítico

| Servicio                        | Tiempo Estimado (default) | Tiempo Optimizado    | Factibilidad              |
| ------------------------------- | ------------------------- | -------------------- | ------------------------- |
| **AWS Textract**                | 33 min                    | 6 min (600 jobs)     | ✅ Excelente              |
| **Azure Document Intelligence** | 111 min                   | 28 min (con aumento) | ⚠️ Requiere planificación |
| **Google Document AI**          | ~139 min                  | Variable             | ⚠️ Requiere evaluación    |

---

## 6. Costos Adicionales a Considerar

### 6.1 Almacenamiento

| Componente                  |              Costo Estimado |
| --------------------------- | --------------------------: |
| MinIO (self-hosted)         | Incluido en infraestructura |
| S3 (si se usa con Textract) |              ~$0.023/GB/mes |
| Azure Blob Storage          |              ~$0.018/GB/mes |

**Estimación:** 100K PDFs × 500KB promedio = 50GB → ~$1-2/mes

### 6.2 Transferencia de Datos

| Servicio |                   Egress |
| -------- | -----------------------: |
| AWS      | $0.09/GB (primeros 10TB) |
| Azure    |                $0.087/GB |
| Google   |                 $0.12/GB |

**Estimación:** Datos extraídos ~100MB → Despreciable

### 6.3 Desarrollo y Pruebas

- Free tier suficiente para desarrollo
- Pruebas de carga: ~$100-500 (1000-5000 E14 de prueba)

---

## 7. Recomendación Final

### Opción Recomendada: AWS Textract

**Razones:**

1. **Capacidad de concurrencia superior**: 100-600 jobs paralelos vs 15 TPS de Azure
2. **Sin costo adicional por async**: El procesamiento paralelo no tiene sobrecosto
3. **Tiempo de procesamiento**: 6-33 minutos para 100K documentos
4. **Escalabilidad**: Incremento de cuotas sin costo adicional
5. **Integración**: API bien documentada, SDKs maduros

**Costo Estimado Final (100K E14 × 4 páginas promedio):**

| Concepto                                      |        Costo |
| --------------------------------------------- | -----------: |
| Extracción de formularios (400K págs × $0.05) |      $20,000 |
| Almacenamiento S3 (50GB × 1 mes)              |        $1.15 |
| Transferencia de datos                        |          ~$0 |
| **Total por escrutinio**                      | **~$20,000** |

### Alternativa: Azure Document Intelligence

Considerar si:

- Ya existe infraestructura Azure
- Se requiere modelo de precios predecible
- Se puede solicitar aumento de cuotas con anticipación

**Costo estimado:** $8,600-16,600 (dependiendo del modelo de precios negociado)

---

## 8. Próximos Pasos

- [ ] Definir número promedio de páginas por E14 según tipo de elección
- [ ] Realizar prueba de concepto con 100-1000 E14 reales
- [ ] Solicitar aumento de cuotas en el servicio elegido
- [ ] Evaluar precisión de extracción con formularios E14 reales
- [ ] Diseñar arquitectura de integración MinIO → OCR → micro-scrutiny

---

## 9. Fuentes

- [Azure Document Intelligence Pricing](https://azure.microsoft.com/en-us/pricing/details/document-intelligence/)
- [Azure Document Intelligence Limits](https://learn.microsoft.com/en-us/azure/ai-services/document-intelligence/service-limits)
- [AWS Textract Pricing](https://aws.amazon.com/textract/pricing/)
- [AWS Textract Quotas](https://docs.aws.amazon.com/textract/latest/dg/limits.html)
- [Google Document AI Pricing](https://cloud.google.com/document-ai/pricing)
