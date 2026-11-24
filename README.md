# Pedidos360

Aplicación full-stack para la gestión de pedidos con frontend React/Vite, autenticación Microsoft Entra ID y una arquitectura de BFF con microservicios Spring Boot.

## 1. Componentes

| Componente | Puerto local | Responsable |
|---|---:|---|
| Frontend React/Vite | 5173 | Interfaz web y autenticación MSAL |
| BFF | 8080 | Seguridad, orquestación y comunicación con los micros |
| `ms-clientes` | 8081 | CRUD y consulta de clientes |
| `ms-pedidos` | 8082 | CRUD y consulta de pedidos |
| `ms-pedidos360-catalog` | 8083 | Catálogo de productos |
| `ms-pedidos360-notify` | 8084 | Notificaciones por correo y procesamiento de comandos RabbitMQ |
| `ms-pedidos360-report` | 8085 | KPIs y reportes de ventas, estados y tiempos de entrega |
| `ms-pedidos360-audit` | 8086 | Registro y consulta de auditoría de pedidos |

El frontend se comunica con el BFF a través del API Gateway o directamente con la instancia configurada del BFF. Los microservicios no deben exponerse al navegador.

## 2. Arquitectura de integración

- El BFF valida el token JWT emitido por Entra ID.
- `ms-pedidos360-audit` consume eventos desde Kafka y persiste el historial en MySQL.
- `ms-pedidos360-report` consume eventos desde Kafka/RabbitMQ y genera sus datos de reporte.
- `ms-pedidos360-notify` escucha comandos de RabbitMQ y procesa correos, pedidos y facturas.
- RabbitMQ usa las colas `q.cmd.email`, `q.cmd.pedidos` y `q.cmd.invoice`, con colas DLQ asociadas.
- Kafka usa los topics `orders.events` y `audit.timeline`.

## 3. Requisitos

- Node.js 18 o superior
- npm
- Java 17 o superior
- Maven Wrapper incluido en cada proyecto
- Docker y Docker Compose
- MySQL para `ms-pedidos360-audit` y `ms-pedidos360-report`
- RabbitMQ para notificaciones y reportes
- Kafka para eventos de pedidos y auditoría
- Cuenta válida del tenant de Microsoft Entra ID

## 4. Estructura del repositorio

```text
entra-demo/
├── src/                         # Frontend React
├── api/api/                     # BFF
├── ms-clientes/                 # Microservicio de clientes
├── ms-pedidos/ms-pedidos/       # Microservicio de pedidos
├── ms-pedidos360-catalog/       # Microservicio de catálogo
├── ms-pedidos360-notify/        # Microservicio de notificaciones
├── ms-pedidos360-report/        # Microservicio de reportes
├── ms-pedidos360-audit/         # Microservicio de auditoría
└── kafka/                       # Scripts de inicialización de Kafka
```

## 5. Arranque local del frontend

Desde la raíz del proyecto:

```powershell
cd entra-demo
npm install
npm run dev
```

Abrir `http://localhost:5173`.

Variables usadas por el frontend:

- `VITE_ENTRA_TENANT_ID`
- `VITE_SPA_CLIENT_ID`
- `VITE_API_CLIENT_ID`
- `VITE_BFF_BASE_URL`

La redirección de MSAL se configura como `window.location.origin + '/redirect.html'`.

## 6. Arranque local del backend

Ejecutar cada microservicio desde su carpeta. Las configuraciones tienen valores locales por defecto.

```powershell
cd entra-demo\api\api
.\mvnw.cmd spring-boot:run

cd ..\..\ms-clientes
.\mvnw.cmd spring-boot:run

cd ..\ms-pedidos\ms-pedidos
.\mvnw.cmd spring-boot:run

cd ..\..\ms-pedidos360-catalog
.\mvnw.cmd spring-boot:run

cd ..\ms-pedidos360-notify
.\mvnw.cmd spring-boot:run

cd ..\ms-pedidos360-report
.\mvnw.cmd spring-boot:run

cd ..\ms-pedidos360-audit
.\mvnw.cmd spring-boot:run
```

`ms-pedidos360-notify` incluye Docker Compose:

```powershell
cd entra-demo\ms-pedidos360-notify
docker compose up --build
```

### Variables principales

BFF:

- `MS_CLIENTES_URL`
- `MS_PEDIDOS_URL`
- `MS_CATALOGO_URL`
- `MS_REPORT_URL`
- `MS_AUDIT_URL`
- `ENTRA_ISSUER_URI`
- `ENTRA_API_CLIENT_ID`
- `INTERNAL_API_KEY`

Notificaciones:

- `SPRING_RABBITMQ_HOST`
- `SPRING_RABBITMQ_PORT`
- `SPRING_RABBITMQ_USERNAME`
- `SPRING_RABBITMQ_PASSWORD`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MS_CLIENTES_URL`

Reportes:

- `REPORT_DB_URL`
- `REPORT_DB_USERNAME`
- `REPORT_DB_PASSWORD`
- `RABBITMQ_HOST`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`

Auditoría:

- `AUDIT_DB_URL`
- `AUDIT_DB_USERNAME`
- `AUDIT_DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`

No agregues credenciales reales a `application.yml`, a este README ni al control de versiones. Usa variables de entorno o un administrador de secretos.

## 7. Endpoints principales

Los endpoints de reportes y auditoría pasan por el BFF:

- `GET /api/report/kpis`
- `GET /api/report/ventas/hora`
- `GET /api/report/productos/top`
- `GET /api/report/pedidos/estados`
- `GET /api/report/tiempos-entrega`
- `GET /api/audit`
- `GET /api/audit/pedido/{pedidoId}`
- `GET /api/audit/{id}`
- `GET /api/notificaciones/health`

Ejemplos directos de los micros:

```text
http://localhost:8085/api/report/kpis
http://localhost:8086/api/audit
http://localhost:8084/api/notificaciones/health
```

## 8. Mensajería

Inicializar los topics de Kafka:

```bash
cd entra-demo/kafka
chmod +x init-topics.sh
./init-topics.sh
```

Para usar otro broker:

```bash
KAFKA_BOOTSTRAP_SERVERS=10.0.0.10:9092 ./init-topics.sh
```

## 9. Flujo normal de uso

1. Levantar Kafka y RabbitMQ.
2. Levantar MySQL y las bases usadas por reportes y auditoría.
3. Levantar los microservicios dependientes.
4. Levantar el BFF en el puerto `8080`.
5. Levantar el frontend en `5173`.
6. Iniciar sesión con una cuenta del tenant.
7. Usar el frontend para crear o consultar pedidos y visualizar reportes o auditoría.

## 10. Build y pruebas

Frontend:

```powershell
cd entra-demo
npm run build
npm run preview
```

Microservicios:

```powershell
cd entra-demo\ms-pedidos360-notify
.\mvnw.cmd test

cd ..\ms-pedidos360-report
.\mvnw.cmd test

cd ..\ms-pedidos360-audit
.\mvnw.cmd test
```

## 11. Despliegue en AWS EC2

Los servicios pueden ejecutarse como JAR con `java -jar` o como servicios `systemd`.

Ejemplo:

```bash
nohup java -jar ms-pedidos360-notify.jar --server.port=8084 > /var/log/ms-pedidos360-notify.log 2>&1 &
nohup java -jar ms-pedidos360-report.jar --server.port=8085 > /var/log/ms-pedidos360-report.log 2>&1 &
nohup java -jar ms-pedidos360-audit.jar --server.port=8086 > /var/log/ms-pedidos360-audit.log 2>&1 &
```

Al desplegar, configura las URLs internas del BFF y cada dependencia con las variables de entorno correspondientes.

## 12. Solución de problemas

### El login falla

- Verificar que el usuario pertenezca al tenant configurado.
- Verificar `VITE_ENTRA_TENANT_ID`, `VITE_SPA_CLIENT_ID` y `VITE_API_CLIENT_ID`.
- Confirmar la redirección `http://localhost:5173/redirect.html`.

### El frontend no obtiene datos

- Verificar que el BFF esté disponible.
- Revisar `VITE_BFF_BASE_URL`.
- Confirmar que el access token se envía en `Authorization: Bearer ...`.
- Revisar los logs y las URLs internas del BFF.

### Notificaciones no llegan

- Verificar RabbitMQ y sus variables de entorno.
- Confirmar las colas `q.cmd.email`, `q.cmd.pedidos` y `q.cmd.invoice`.
- Revisar las DLQ si los mensajes se rechazan.
- Verificar el host, puerto y credenciales SMTP.

### Reportes o auditoría no reciben eventos

- Verificar el broker configurado en `KAFKA_BOOTSTRAP_SERVERS`.
- Confirmar que existan los topics `orders.events` y `audit.timeline`.
- Revisar la conectividad y credenciales de las bases `db_report` y `db_audit`.

### Logs en EC2

```bash
tail -f /var/log/ms-pedidos360-notify.log
tail -f /var/log/ms-pedidos360-report.log
tail -f /var/log/ms-pedidos360-audit.log
tail -f /var/log/bff.log
```
