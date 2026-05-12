# 🛠️ Enterprise Service Desk API

Una API RESTful robusta y escalable para la gestión de tickets de soporte técnico corporativo. Diseñada con una arquitectura multicapa, este backend garantiza la seguridad, trazabilidad y eficiencia en la atención de incidencias.

## 🚀 Características Principales

*   **Seguridad y Autenticación:** Implementación de Spring Security 6 con JSON Web Tokens (JWT). Arquitectura *Stateless* para máxima escalabilidad horizontal.
*   **Gestión de Tickets (CRUD Completo):** Creación, asignación y seguimiento del ciclo de vida de los tickets (OPEN, IN_PROGRESS, CLOSED).
*   **Trazabilidad y Auditoría:** Sistema inmutable de historial de tickets (`TicketHistory`) que registra automáticamente quién, cuándo y qué acción se realizó sobre cada incidencia.
*   **Consultas Optimizadas:** Paginación desde el motor de base de datos y filtros dinámicos mediante Spring Data JPA. Mitigación del problema N+1 mediante `JOIN FETCH`.
*   **Manejo Global de Excepciones:** Respuestas estandarizadas y limpias para el cliente mediante `@RestControllerAdvice`, protegiendo la infraestructura interna.
*   **Validación de Datos:** Uso de `jakarta.validation` para asegurar la integridad de la información entrante mediante DTOs.

## 💻 Stack Tecnológico

*   **Lenguaje:** Java 17+
*   **Framework:** Spring Boot 3.x
*   **Seguridad:** Spring Security + JJWT (JSON Web Tokens)
*   **Persistencia:** Spring Data JPA / Hibernate
*   **Base de Datos:** PostgreSQL
*   **Infraestructura:** Docker & Docker Compose
*   **Gestor de Dependencias:** Maven
*   **Herramientas Auxiliares:** Lombok, MapStruct (o mapeo manual)

## 🏗️ Arquitectura

El proyecto sigue una arquitectura estricta de N-Capas (N-Tier Architecture):
1.  **Controllers:** Exposición de endpoints RESTful.
2.  **Services:** Contiene la lógica de negocio, validaciones de estado y orquestación.
3.  **Repositories:** Capa de acceso a datos interactuando con PostgreSQL.
4.  **DTOs & Mappers:** Aislamiento de las entidades de dominio para evitar fuga de información sensible.

## ⚙️ Requisitos Previos e Instalación

Para ejecutar este proyecto en local, necesitas tener instalado:
*   Java Development Kit (JDK) 17 o superior.
*   Docker y Docker Compose (para levantar la base de datos).
*   Maven.

### Pasos para levantar el entorno:

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/ing-ronnychiclla/service-desk-api.git
   cd service-desk-api

Levantar la Base de Datos:
docker-compose up -d
Compilar y ejecutar la aplicación:
mvn clean install
mvn spring-boot:run
La API estará disponible en: http://localhost:8080/api/v1📡 

Endpoints Principales🔐
🔐 Autenticación (/api/v1/auth)

| Método | Endpoint    | Descripción |
|---------|-------------|-------------|
| POST    | /register   | Registra un nuevo usuario y devuelve un token JWT. |
| POST    | /login      | Valida credenciales y devuelve el token de acceso. |

🎫 Tickets (/api/v1/tickets) (Requieren Token JWT)

| Método | Endpoint | Descripción |
|---------|----------|-------------|
| `POST` | `/` | Crea un nuevo ticket de soporte. |
| `GET` | `/?page=0&size=10&status=OPEN` | Lista tickets paginados y con filtros dinámicos. |
| `PATCH` | `/{id}/assign` | Asigna un ticket a un agente específico. |
| `GET` | `/{id}/history` | Obtiene la bitácora de auditoría del ticket. |

👨‍💻 Autor  
Ronny Chiclla Zamora - Software Engineer

- LinkedIn: [Ronny Chiclla](https://www.linkedin.com/in/ing-ronnychiclla-c-7a146a260/)
- GitHub: [ing-ronnychiclla](https://github.com/ing-ronnychiclla)

Este proyecto fue construido aplicando principios SOLID y estándares de diseño de APIs RESTful orientados al entorno corporativo.