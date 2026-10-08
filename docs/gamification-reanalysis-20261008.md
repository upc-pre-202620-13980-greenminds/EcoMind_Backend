# Gamification — revisión del informe y legacy (2026-10-08)

## Autoridad y alcance

Informe `GreenMinds_Report origin/develop` **0d0783341b9f0b77883c65068d0af4809a74f5b9**:
sección 2.6.6, tablas 89–102, HU-040/HU-041 y TS-006/TS-007.
Se inspeccionaron el texto, PlantUML, SQL, DSL C4 y los diagramas de clases y componentes renderizados.
La revisión corresponde al backend de Gamification; no declara terminada toda la entrega académica.

Referencias de estructura: legacy `GreeenMinds/EcoMind_backend`, especialmente
`ranking/interfaces/rest/RankingsController`, su assembler y `quests/application/internal/services/QuestRewardService`;
`ICEQ2026/coldtrace-platform`, assembler de resultados de consultas de Reports.
Se conservan sus capas, servicios de comandos/consultas, inyección por constructor, records y
transformadores estáticos. Las reglas actuales del informe tienen prioridad sobre el comportamiento legacy.

## Hallazgos corregidos

| Estado | Regla y fuente | Evidencia encontrada | Remediación y verificación |
|---|---|---|---|
| Corregido | Tabla 96: resources y assemblers REST; controller/assembler legacy de Ranking | Ranking y el historial por periodo serializaban directamente read models y value objects del dominio | `PageResource`, resources de ranking e historial y assemblers dedicados; se mantiene el JSON existente, cantidades base/efectivas, beneficiario y paginación |
| Corregido | Tabla 96: errores HTTP uniformes; assembler de Result de ColdTrace | El alias user_achievement devolvía ApplicationError directamente para titular ajeno | Usa el ResponseEntityAssembler compartido y ErrorResource; la prueba verifica 403, código público y ausencia de la categoría interna |
| Corregido | Tabla 89 y diagrama de clases: UserProgress contiene Streak | El agregado reconstruía Streak desde cuatro campos propios en cada operación | El agregado conserva el value object Streak; getters y mapeo JPA mantienen su contrato; pruebas de actividad, protección y recuperación de varios días |
| Corregido | Tabla 99: reconocimiento familiar sin repetir recompensas de quests | El consumidor publicado evaluaba el plan otra vez después de que FamilyRewardCommandService ya lo reconociera | Un solo coordinador del reconocimiento; prueba del evento real repetido con diferentes eventId: una recompensa familiar, un hito y un logro, sin alterar el premio individual previo |
| Corregido | Identidad canónica positiva; adaptación documentada del modelo UUID al backend numérico | Contratos de meta/evento/publicación y AchievementAward admitían communityId cero o negativo | Validación en los contratos y agregado; prueba rechaza ambos valores antes de persistir premios o logros |
| Corregido | Tabla 98: participantes autorizados LOCAL; Tabla 101: Community suministra membresías | El proveedor ordenaba varias membresías locales y elegía la primera silenciosamente | Community identifica la ambigüedad; su cliente ACL la traduce a dependencia no disponible. REST devuelve 503 sin fabricar un alcance ni conceder recompensas; prueba con el proveedor real |

## Reglas contrastadas con la implementación

| Estado | Regla/fuente | Implementación y evidencia ejecutable |
|---|---|---|
| Cumple en backend | Tablas 89–90: seis orígenes, beneficiarios USER/FAMILY y premio por ejecución canónica | Servicios individuales/familiares, ledger compartido y constraint de origen. Pruebas de duplicados, concurrencia, aislamiento y rollback |
| Cumple en backend | Tabla 99: finalización y recompensa en la misma transacción | Consumidores reales de Quests con transacción obligatoria; score, logro y outbox revierten conjuntamente |
| Cumple en backend | Tabla 93: multiplicador XP vigente; HU-041 | Multiplica únicamente XP; curva aprobada 100/80/50/20/0 dentro de tres horas; historial validado de Quests incluye intentos anteriores y de recompensa cero |
| Cumple en backend | Tabla 89: primer reto diario válido, protección confirmada, fallo técnico pendiente | Streak, cierre de días, solicitudes y resultados correlacionados; inventario real de Monetization. Recuperación de varios días preserva actividad original y consume un protector por fecha |
| Cumple en backend | Tablas 89/97/99: familia y criterios configurados de logros | Familia recibe solo bonus adicional configurado; el evento publicado actual no define bonus y registra cero. Reconocimiento por hito sin duplicar recompensas individuales |
| Cumple como contrato consumidor | HU-040 y Tabla 99: insignias de participantes elegibles de metas cumplidas | CommunityProgressConsumer reconoce comunidad y participantes sin inventar premio; no trata una participación en evento como meta cumplida. Falta el productor real de Community |
| Cumple en backend | Tablas 89/92/99: aviso distinto de publicación voluntaria | Aviso individual separado de post; titular/membresía/permisos desde JWT; requestId estable, estado pendiente y confirmación exacta de publicación persistida en Community |
| Cumple en backend | TS-006 y Tablas 95–98: catálogo, concesiones y filtros por alcance | Rutas modernas y aliases; consultas propias, familiares y comunitarias autorizadas; filtrar no cambia el alcance del beneficiario ni concede premios |
| Cumple en backend | TS-007 y Tabla 98: participantes y transacciones, posiciones en Android | GLOBAL/LOCAL/FRIENDS/FAMILIES; fechas [from,to), paginación estable y hasNext; no se guarda ni calcula una posición semanal en el backend |
| Cumple en backend | Tabla 100: capas, ACL y persistencia propia | Dominio sin anotaciones JPA; puertos de repositorio, adapters y clientes de contratos públicos. Sin SQL contra tablas de otros bounded contexts; versiones y constraints propias en PostgreSQL |

## Diferencias que se conservan por decisión documentada

- El legacy multiplica ecopoints y tiene otra curva de repetición. El informe actual exige XP y
  disminución en cada repetición; se conserva la curva aprobada por el usuario.
- Los IDs de usuarios, familias, comunidades y posts son numéricos en los proveedores publicados.
  Se utilizan esas identidades; las ejecuciones normalizadas, concesiones y solicitudes mantienen UUID.
- Algunos consumidores/controllers agrupan responsabilidades de varias clases propuestas por el informe.
  Esto no mezcla dominio, persistencia y REST ni traslada reglas al cliente.
- El texto de Tabla 94 menciona calcular posiciones, pero TS-007 y Tablas 89/98/102 asignan
  las posiciones semanales a Android. Se sigue esa responsabilidad explícita.

## Pendientes reales y responsables

| Estado | Fuente y evidencia | Remediación |
|---|---|---|
| Falta configuración productiva | Quests publicado no proporciona XP base; el informe no define sus valores. El catálogo de logros también es configurable, sin umbrales de producción inventados | Acordar/configurar XP por versión y catálogo. ConfigureQuestExperienceCommand requiere valores explícitos; una finalización sin XP falla y revierte |
| Falta productor externo | Community publicado tiene membresías, pero no implementación de cumplimiento de metas/eventos | Community debe emitir hechos validados con ejecución, participantes y recompensa opcional; consumidores Gamification están probados con contratos explícitos |
| Inconsistencia del proveedor | Community permite crear varias comunidades locales como ADMIN aunque JoinCommunity rechaza una segunda membresía local | Community debe definir/corregir esa pertenencia; Gamification devuelve 503 ante datos ambiguos. No elige ni elimina membresías automáticamente |
| Inconsistencia documental | SQL/C4 de Gamification siguen indicando MySQL y UUID externos, mientras develop usa PostgreSQL e IDs numéricos | Actualizar artefactos físicos del informe al integrar el equipo. Se conserva la edición local existente del README |
| Fuera del backend Gamification | HU-040: animación, anuncio colectivo/feed e hitos 25/50/75%; HU-041: mensaje para practicar otros juegos; Tabla 102: Android | Community y clientes móviles implementan presentación, feed y productores. El backend conserva premios efectivos/cero y concesiones consultables; esas pantallas no se declaran listas |

La matriz de cobertura ampliada permanece en [gamification-report-coverage.md](gamification-report-coverage.md).
Los resultados de ejecución de esta revisión se conservan fuera del repositorio, en la evidencia local.
