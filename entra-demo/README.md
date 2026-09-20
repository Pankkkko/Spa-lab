# Estructura del Proyecto Pedidos360

> **Evaluación Parcial N° 1** — Desarrollo Cloud Native I
> Arquitectura: BFF + Microservicios + Frontend + AWS

---

## 1. Visión General

```text
React (localhost:5173)
   │
   │  Authorization: Bearer <access_token>
   ▼
BFF (localhost:8080)
   │  Valida JWT (scope + roles)
   │
   ├──► ms-clientes (localhost:8081) ──► RDS MySQL (AWS)
   │
   └──► ms-pedidos  (localhost:8082) ──► RDS MySQL (AWS)
```

### Reglas clave

- El **frontend nunca** llama directamente a los microservicios.
- El **BFF** es el único punto de entrada al backend.
- Cada microservicio tiene su **propia base de datos** (o esquema).
- Todos los componentes se despliegan en **AWS** (EC2 + RDS + API Gateway).

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

### Frontend (React)

- Login con MSAL (Entra ID).
- Obtener access token para el BFF.
- Adjuntar token en cada llamada (header `Authorization`).
- Mostrar datos de clientes y pedidos.
- **No** valida JWT (lo hace el BFF).
- **No** conoce los microservicios.

### BFF (bff-pedidos360)

- Valida el JWT (firma, issuer, audiencia, vigencia).
- Verifica scope (`pedidos.read`, `pedidos.write`).
- Verifica rol (`Admin`, `Cliente`).
- Orquesta llamadas a `ms-clientes` y `ms-pedidos`.
- Combina respuestas y las adapta para el frontend.
- Aplica CORS para permitir `localhost:5173`.

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

> Todas las peticiones del frontend van al BFF (`http://localhost:8080`).
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
VITE_BFF_BASE_URL=http://localhost:8080
```

### BFF (PowerShell)

```powershell
$env:ENTRA_ISSUER_URI="https://login.microsoftonline.com/49551105-7651-4748-a01b-22b28daeb087/v2.0"
$env:ENTRA_API_CLIENT_ID="fa47563a-a68d-4d29-93c1-64bbfab52085"
$env:MS_CLIENTES_URL="http://localhost:8081"
$env:MS_PEDIDOS_URL="http://localhost:8082"
$env:DB_CLIENTES_URL="jdbc:mysql://<RDS_HOST>:3306/pedidos360_clientes"
$env:DB_PEDIDOS_URL="jdbc:mysql://<RDS_HOST>:3306/pedidos360_pedidos"
$env:DB_USERNAME="admin"
$env:DB_PASSWORD="<password>"
```

### ms-clientes (PowerShell)

```powershell
$env:DB_CLIENTES_URL="jdbc:mysql://<RDS_HOST>:3306/pedidos360_clientes"
$env:DB_USERNAME="admin"
$env:DB_PASSWORD="<password>"
```

### ms-pedidos (PowerShell)

```powershell
$env:DB_PEDIDOS_URL="jdbc:mysql://<RDS_HOST>:3306/pedidos360_pedidos"
$env:DB_USERNAME="admin"
$env:DB_PASSWORD="<password>"
```

---

## 6. Puertos y Servicios

| Servicio | Puerto | Comando de arranque |
|----------|--------|---------------------|
| Frontend React | 5173 | `npm run dev -- --port 5173 --strictPort` |
| BFF | 8080 | `.\mvnw.cmd spring-boot:run` |
| ms-clientes | 8081 | `.\mvnw.cmd spring-boot:run` |
| ms-pedidos | 8082 | `.\mvnw.cmd spring-boot:run` |
| RDS MySQL | 3306 | (servicio gestionado en AWS) |

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

## 9. Despliegue en AWS (Fase Posterior)

### Componentes a desplegar

- **RDS MySQL**: dos esquemas (`pedidos360_clientes`, `pedidos360_pedidos`).
- **EC2**: una instancia por microservicio + BFF (o una instancia con varios JARs).
- **API Gateway**: punto de entrada público con validación JWT.
- **Certificado HTTPS**: para el dominio del API Gateway.

### Flujo en producción

```text
React ──► API Gateway (HTTPS) ──► BFF (EC2) ──► ms-clientes + ms-pedidos (EC2) ──► RDS
```

---

## 10. Checklist de Entrega

- [ ] Los 4 proyectos compilan sin errores.
- [ ] El frontend hace login con MSAL.
- [ ] El frontend adjunta el token en cada llamada al BFF.
- [ ] El BFF valida issuer, audiencia, firma y vigencia.
- [ ] El BFF valida scope (`pedidos.read`, `pedidos.write`).
- [ ] El BFF valida rol (`Admin`, `Cliente`).
- [ ] El BFF orquesta llamadas a `ms-clientes` y `ms-pedidos`.
- [ ] Los microservicios leen/escriben en RDS MySQL.
- [ ] CORS configurado en el BFF para `localhost:5173`.
- [ ] `.gitignore` configurado en cada repo.
- [ ] Repositorios en GitHub con README.
- [ ] Despliegue en AWS funcional (EC2 + RDS + API Gateway).

---

**FIN DEL DOCUMENTO**