# Estructura del Proyecto Pedidos360

> **Evaluación Parcial N° 1** — Desarrollo Cloud Native I
> Arquitectura: BFF + Microservicios + Frontend + AWS

---

## 1. Visión General

El sistema **Pedidos360** se compone de cuatro componentes desplegados en AWS:

1. **Frontend** (React/Angular + MSAL) → servido por Nginx en una instancia EC2.
2. **API Gateway** → punto de entrada público con HTTPS y validación JWT.
3. **BFF (Backend for Frontend)** → Spring Boot que valida JWT y orquesta microservicios.
4. **Microservicios** → `ms-clientes` y `ms-pedidos`, cada uno en su propia EC2 con su propia base de datos RDS.

```text
┌──────────────────────────────────────────────────────────────────────┐
│                              AWS VPC                                 │
│                                                                      │
│  ┌────────────────┐                                                  │
│  │  EC2 #1        │  Frontend (React/Angular + Nginx)                │
│  │  :80/:443      │                                                  │
│  └───────┬────────┘                                                  │
│          │ HTTPS                                                     │
│          ▼                                                            │
│  ┌────────────────┐                                                  │
│  │  API Gateway   │  Valida JWT + HTTPS                              │
│  └───────┬────────┘                                                  │
│          │                                                            │
│          ▼                                                            │
│  ┌────────────────┐                                                  │
│  │  EC2 #2        │  BFF (Spring Boot) :8080                         │
│  └───┬────────┬───┘                                                  │
│      │        │                                                      │
│      ▼        ▼                                                      │
│  ┌─────────┐  ┌─────────┐                                            │
│  │ EC2 #3  │  │ EC2 #4  │                                            │
│  │ms-client│  │ms-pedid │                                            │
│  │  :8081  │  │  :8082  │                                            │
│  └────┬────┘  └────┬────┘                                            │
│       │            │                                                 │
│       ▼            ▼                                                 │
│  ┌─────────────────────────────────┐                                 │
│  │      RDS MySQL (AWS)            │                                 │
│  │  pedidos360_clientes            │                                 │
│  │  pedidos360_pedidos             │                                 │
│  └─────────────────────────────────┘                                 │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

### Reglas clave

- El **frontend nunca** llama directamente a los microservicios.
- El **API Gateway** es el único punto de entrada público al backend.
- El **BFF** valida el JWT y orquesta las llamadas a los microservicios.
- Cada microservicio tiene su **propia base de datos** (o esquema) en RDS.
- Los microservicios **no validan JWT** (confían en el BFF).
- Todo está en **AWS VPC** con subredes públicas y privadas.

---

## 2. Estructura de Carpetas

```text
pedidos360/
│
├── backend/
│   │
│   ├── bff-pedidos360/                  # Spring Boot — Puerto 8080
│   │   ├── src/main/java/cl/duoc/bff/
│   │   │   ├── BffApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── RestClientConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── ClienteController.java
│   │   │   │   └── PedidoController.java
│   │   │   ├── service/
│   │   │   │   ├── ClienteService.java
│   │   │   │   └── PedidoService.java
│   │   │   ├── repository/
│   │   │   │   ├── ClienteRepository.java
│   │   │   │   └── PedidoRepository.java
│   │   │   └── dto/
│   │   │       ├── ClienteResponse.java
│   │   │       └── PedidoResponse.java
│   │   ├── src/main/resources/
│   │   │   └── application.yml
│   │   └── pom.xml
│   │
│   ├── ms-clientes/                     # Spring Boot — Puerto 8081
│   │   ├── src/main/java/cl/duoc/msclientes/
│   │   │   ├── MsClientesApplication.java
│   │   │   ├── controller/
│   │   │   │   └── ClienteController.java
│   │   │   ├── service/
│   │   │   │   └── ClienteService.java
│   │   │   ├── repository/
│   │   │   │   └── ClienteRepository.java
│   │   │   ├── entity/
│   │   │   │   └── Cliente.java
│   │   │   └── dto/
│   │   │       └── ClienteResponse.java
│   │   ├── src/main/resources/
│   │   │   └── application.yml
│   │   └── pom.xml
│   │
│   └── ms-pedidos/                      # Spring Boot — Puerto 8082
│       ├── src/main/java/cl/duoc/mspedidos/
│       │   ├── MsPedidosApplication.java
│       │   ├── controller/
│       │   │   └── PedidoController.java
│       │   ├── service/
│       │   │   └── PedidoService.java
│       │   ├── repository/
│       │   │   └── PedidoRepository.java
│       │   ├── entity/
│       │   │   └── Pedido.java
│       │   └── dto/
│       │       └── PedidoResponse.java
│       ├── src/main/resources/
│       │   └── application.yml
│       └── pom.xml
│
├── frontend/                            # React + Vite + MSAL
│   ├── src/
│   │   ├── main.tsx
│   │   ├── App.tsx
│   │   ├── authConfig.ts
│   │   ├── token.ts
│   │   ├── api.ts
│   │   ├── components/
│   │   │   ├── LoginButton.tsx
│   │   │   ├── ClienteView.tsx
│   │   │   └── PedidoView.tsx
│   │   └── vite-env.d.ts
│   ├── public/
│   ├── index.html
│   ├── redirect.html
│   ├── vite.config.ts
│   ├── .env.local
│   └── package.json
│
├── .gitignore                           # node_modules, target, .env.local
├── CONTRATO_API.md                      # contrato entre frontend y BFF
├── README.md
└── ESTRUCTURA.md                        # este archivo
```

---

## 3. Responsabilidades por Componente

### Frontend (React/Angular)

- Login con MSAL (Entra ID).
- Obtener access token para el API Gateway.
- Adjuntar token en cada llamada (header `Authorization`).
- Mostrar datos de clientes y pedidos.
- **No** valida JWT (lo hace el API Gateway / BFF).
- **No** conoce los microservicios.

### API Gateway (AWS)

- Punto de entrada público.
- Termina HTTPS.
- Valida JWT (opcional, delega en el BFF).
- Redirige al BFF.

### BFF (bff-pedidos360)

- Valida el JWT (firma, issuer, audiencia, vigencia).
- Verifica scope (`pedidos.read`, `pedidos.write`).
- Verifica rol (`Admin`, `Cliente`).
- Orquesta llamadas a `ms-clientes` y `ms-pedidos`.
- Combina respuestas y las adapta para el frontend.
- Aplica CORS.

### ms-clientes

- CRUD de clientes.
- Datos en **RDS MySQL** (tabla `clientes`).
- No valida JWT (confía en el BFF).
- Endpoint: `GET /api/clientes/{id}`

### ms-pedidos

- CRUD de pedidos.
- Datos en **RDS MySQL** (tabla `pedidos`).
- No valida JWT (confía en el BFF).
- Endpoint: `GET /api/pedidos/cliente/{clienteId}`

---

## 4. Contrato de API (Frontend ↔ BFF)

> Todas las peticiones del frontend van al **API Gateway**, que reenvía al BFF.
> Todas requieren header: `Authorization: Bearer <access_token>`

### 4.1 `GET /api/clientes/{id}`

**Descripción:** Obtiene un cliente por su ID.
**Requiere:** Scope `pedidos.read` **o** rol `Admin`.

**Respuesta 200:**

```json
{
  "id": 1,
  "nombre": "Wacoldo Soto",
  "email": "waco.soto@duocuc.cl",
  "telefono": "+56912345678"
}
```

### 4.2 `GET /api/pedidos/cliente/{clienteId}`

**Descripción:** Obtiene los pedidos de un cliente.
**Requiere:** Scope `pedidos.read` **o** rol `Admin`.

**Respuesta 200:**

```json
[
  {
    "id": 1,
    "clienteId": 1,
    "producto": "Notebook Lenovo",
    "cantidad": 1,
    "total": 599990,
    "fecha": "2026-09-15"
  },
  {
    "id": 2,
    "clienteId": 1,
    "producto": "Mouse Logitech",
    "cantidad": 2,
    "total": 39980,
    "fecha": "2026-09-16"
  }
]
```

### 4.3 `GET /api/clientes/{id}/detalle`

**Descripción:** Combina cliente + sus pedidos en una sola respuesta.
**Requiere:** Scope `pedidos.read` **o** rol `Admin`.

**Respuesta 200:**

```json
{
  "id": 1,
  "nombre": "Wacoldo Soto",
  "email": "waco.soto@duocuc.cl",
  "telefono": "+56912345678",
  "pedidos": [
    {
      "id": 1,
      "producto": "Notebook Lenovo",
      "cantidad": 1,
      "total": 599990,
      "fecha": "2026-09-15"
    },
    {
      "id": 2,
      "producto": "Mouse Logitech",
      "cantidad": 2,
      "total": 39980,
      "fecha": "2026-09-16"
    }
  ]
}
```

### 4.4 `POST /api/pedidos`

**Descripción:** Crea un nuevo pedido.
**Requiere:** Scope `pedidos.write` **Y** rol `Admin`.

**Body:**

```json
{
  "clienteId": 1,
  "producto": "Teclado Mecánico",
  "cantidad": 1,
  "total": 89990
}
```

**Respuesta 201:**

```json
{
  "id": 3,
  "clienteId": 1,
  "producto": "Teclado Mecánico",
  "cantidad": 1,
  "total": 89990,
  "fecha": "2026-09-19"
}
```

### 4.5 Códigos de error comunes

| Código | Significado |
|--------|-------------|
| `401 Unauthorized` | No se envió token o es inválido. |
| `403 Forbidden` | Token válido pero sin scope/rol suficiente. |
| `404 Not Found` | El recurso solicitado no existe. |
| `500 Server Error` | Error interno en el BFF o en un microservicio. |

---

## 5. Variables de Entorno

### Frontend (`.env.local`)

```env
VITE_ENTRA_TENANT_ID=49551105-7651-4748-a01b-22b28daeb087
VITE_SPA_CLIENT_ID=<SPA_CLIENT_ID>
VITE_API_CLIENT_ID=fa47563a-a68d-4d29-93c1-64bbfab52085
VITE_API_BASE_URL=https://<api-gateway-url>
```

### BFF (EC2 — variables de entorno del sistema)

```bash
ENTRA_ISSUER_URI=https://login.microsoftonline.com/49551105-7651-4748-a01b-22b28daeb087/v2.0
ENTRA_API_CLIENT_ID=fa47563a-a68d-4d29-93c1-64bbfab52085
MS_CLIENTES_URL=http://<ip-privada-ec2-3>:8081
MS_PEDIDOS_URL=http://<ip-privada-ec2-4>:8082
DB_CLIENTES_URL=jdbc:mysql://<rds-host>:3306/pedidos360_clientes
DB_PEDIDOS_URL=jdbc:mysql://<rds-host>:3306/pedidos360_pedidos
DB_USERNAME=admin
DB_PASSWORD=<password>
```

### ms-clientes (EC2 — variables de entorno del sistema)

```bash
DB_CLIENTES_URL=jdbc:mysql://<rds-host>:3306/pedidos360_clientes
DB_USERNAME=admin
DB_PASSWORD=<password>
```

### ms-pedidos (EC2 — variables de entorno del sistema)

```bash
DB_PEDIDOS_URL=jdbc:mysql://<rds-host>:3306/pedidos360_pedidos
DB_USERNAME=admin
DB_PASSWORD=<password>
```

---

## 6. Puertos y Servicios

| Servicio | Puerto | Hosting | Comando |
|----------|--------|---------|---------|
| Frontend (build) | 80/443 | EC2 #1 (Nginx) | `nginx` |
| API Gateway | 443 | AWS managed | — |
| BFF | 8080 | EC2 #2 | `java -jar bff-pedidos360.jar` |
| ms-clientes | 8081 | EC2 #3 | `java -jar ms-clientes.jar` |
| ms-pedidos | 8082 | EC2 #4 | `java -jar ms-pedidos.jar` |
| RDS MySQL | 3306 | AWS managed | — |

---

## 7. Scopes y Roles (Entra ID)

### Scopes definidos en la API

| Scope | Descripción |
|-------|-------------|
| `pedidos.read` | Permite leer clientes y pedidos. |
| `pedidos.write` | Permite crear y modificar pedidos. |

### App Roles definidos

| Rol | Descripción |
|-----|-------------|
| `Admin` | Acceso total al sistema. |
| `Cliente` | Acceso de solo lectura. |

### Validación en el BFF

```java
GET  /api/clientes/**  → hasAnyAuthority("SCOPE_pedidos.read", "ROLE_Admin")
GET  /api/pedidos/**   → hasAnyAuthority("SCOPE_pedidos.read", "ROLE_Admin")
POST /api/pedidos      → hasAuthority("SCOPE_pedidos.write") + hasRole("Admin")
```

---

## 8. Estrategia de Trabajo en GitHub

### Repositorio único (monorepo)

- Rama `main`: solo código estable (**nunca trabajar directo**).
- Ramas por funcionalidad:
  - `backend/ms-clientes`
  - `backend/ms-pedidos`
  - `backend/bff`
  - `frontend/login`
  - `frontend/vistas-clientes`
  - `frontend/vistas-pedidos`

### Flujo

1. Crear rama desde `main`.
2. Trabajar y hacer commits.
3. Abrir **Pull Request** hacia `main`.
4. Revisar y mergear.

### `.gitignore` obligatorio

```gitignore
node_modules/
target/
.env.local
.env
dist/
build/
*.class
.idea/
.vscode/
```

---

## 9. Despliegue en AWS

### 9.1 Arquitectura de red

| Componente | Subred | Security Group | Acceso permitido desde |
|------------|--------|----------------|------------------------|
| EC2 #1 (Frontend) | Pública | `sg-frontend` | Internet (80/443) |
| API Gateway | Pública (managed) | — | Internet (443) |
| EC2 #2 (BFF) | Privada | `sg-bff` | API Gateway |
| EC2 #3 (ms-clientes) | Privada | `sg-ms-clientes` | `sg-bff` |
| EC2 #4 (ms-pedidos) | Privada | `sg-ms-pedidos` | `sg-bff` |
| RDS MySQL | Privada | `sg-rds` | `sg-ms-clientes`, `sg-ms-pedidos` |

### 9.2 Instancias EC2

| # | Nombre | Componente | Tipo sugerido | AMI |
|---|--------|------------|---------------|-----|
| 1 | `ec2-frontend` | React/Angular + Nginx | t3.micro | Amazon Linux 2023 |
| 2 | `ec2-bff` | BFF Spring Boot | t3.small | Amazon Linux 2023 (JDK 25) |
| 3 | `ec2-ms-clientes` | ms-clientes Spring Boot | t3.small | Amazon Linux 2023 (JDK 25) |
| 4 | `ec2-ms-pedidos` | ms-pedidos Spring Boot | t3.small | Amazon Linux 2023 (JDK 25) |

### 9.3 Flujo de despliegue

```text
1. Empaquetar JARs:  ./mvnw clean package
2. Subir JARs a EC2:  scp target/*.jar ec2-user@<ip>:/opt/app/
3. Definir variables de entorno en cada EC2.
4. Arrancar servicios:  java -jar /opt/app/<app>.jar
5. Configurar Nginx en EC2 #1 para servir el build del frontend.
6. Configurar API Gateway para redirigir al BFF.
7. Asociar certificado HTTPS (ACM) al API Gateway.
```

### 9.4 Flujo en producción

```text
Usuario
   │
   ▼ HTTPS
API Gateway ──► BFF (EC2 #2) ──► ms-clientes (EC2 #3) ──► RDS MySQL
                        │
                        └──► ms-pedidos (EC2 #4) ──► RDS MySQL
```

---

## 10. Checklist de Entrega

- [ ] Los 4 proyectos compilan sin errores.
- [ ] El frontend hace login con MSAL.
- [ ] El frontend adjunta el token en cada llamada al API Gateway.
- [ ] El BFF valida issuer, audiencia, firma y vigencia.
- [ ] El BFF valida scope (`pedidos.read`, `pedidos.write`).
- [ ] El BFF valida rol (`Admin`, `Cliente`).
- [ ] El BFF orquesta llamadas a `ms-clientes` y `ms-pedidos`.
- [ ] Los microservicios leen/escriben en RDS MySQL.
- [ ] CORS configurado en el BFF.
- [ ] `.gitignore` configurado en cada repo.
- [ ] Repositorios en GitHub con README.
- [ ] **4 instancias EC2 desplegadas** (Frontend, BFF, ms-clientes, ms-pedidos).
- [ ] **API Gateway configurado** delante del BFF.
- [ ] **RDS MySQL** con dos esquemas creados.
- [ ] **Certificado HTTPS** asociado al API Gateway.
- [ ] **Security Groups** configurados según la sección 9.1.

---

**FIN DEL DOCUMENTO**