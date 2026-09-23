# Pedidos360

Aplicación full-stack con frontend React + Vite + MSAL y backend desplegado en AWS EC2. El frontend corre localmente, mientras que los microservicios y el BFF ya están en la nube y solo requieren que estén levantados.

## 1. Resumen rápido

- Frontend: se ejecuta localmente con Vite
- Login: Microsoft Entra ID (MSAL)
- Tenant: el tenant de esta organización
- Backend: servicios Java en EC2 / AWS
- API principal usada por el frontend: `https://tmbul2u2ic.execute-api.us-east-1.amazonaws.com/lanzar`

La app usa autenticación con MSAL y solicita scopes del API registrado en Entra ID:

- `api://fa47563a-a68d-4d29-93c1-64bbfab52085/pedidos.read`
- `api://fa47563a-a68d-4d29-93c1-64bbfab52085/pedidos.write`

## 2. Requisitos

- Node.js 18+ / 20+
- npm
- Git
- Un usuario válido del tenant de Microsoft Entra ID configurado en la app
- Acceso a la instancia EC2 donde están desplegados los servicios

## 3. Estructura de trabajo del proyecto

```text
entra-demo/
├── src/
├── public/
├── api/
├── .env.local
├── docker-compose.yml
├── package.json
├── vite.config.ts
├── README.md
├── ms-clientes/
├── ms-pedidos/
└── api/
```

## 4. Arranque del frontend local

Desde la raíz del proyecto:

```bash
cd entra-demo
npm install
npm run dev
```

Luego abre en el navegador:

```text
http://localhost:5173
```

Si el proyecto corre en Vite, el puerto por defecto es `5173`.

### Variables de entorno del frontend


```

En esta app, la autenticación se configura en `src/authConfig.ts` usando:

- `VITE_ENTRA_TENANT_ID`
- `VITE_SPA_CLIENT_ID`
- `redirectUri: window.location.origin + '/redirect.html'`

## 5. Backend en AWS EC2

El backend no se ejecuta localmente en esta etapa. Los microservicios y el BFF ya están desplegados en EC2 y solo deben estar levantados.

### Endpoints actuales en uso

El frontend usa una URL pública del BFF:

```text
https://tmbul2u2ic.execute-api.us-east-1.amazonaws.com/lanzar
```

Esto se encuentra en archivos como:

- `src/api/pedidosApi.ts`
- `src/api/adminApi.ts`

### ¿Qué debe estar prendido?

- EC2 con el BFF
- EC2 con `ms-clientes`
- EC2 con `ms-pedidos`
- Base de datos RDS asociada a cada microservicio

### Ejemplo de arranque en cada EC2

Si el servicio está empaquetado como JAR, normalmente se levanta así:

```bash
cd /opt/app
nohup java -jar ms-clientes.jar --server.port=8081 > /var/log/ms-clientes.log 2>&1 &
nohup java -jar ms-pedidos.jar --server.port=8082 > /var/log/ms-pedidos.log 2>&1 &
nohup java -jar bff.jar --server.port=8080 > /var/log/bff.log 2>&1 &
```

Si usas `systemd`, el patrón es:

```bash
sudo systemctl daemon-reload
sudo systemctl start ms-clientes
sudo systemctl start ms-pedidos
sudo systemctl start bff
sudo systemctl status ms-clientes ms-pedidos bff
```

> Ajusta el nombre del servicio a como lo hayas definido en la EC2.

## 6. Login con MSAL y usuarios del tenant

La autenticación se realiza con Microsoft Entra ID y solo funciona para usuarios que existan en este tenant.

### Importante

- El usuario debe estar registrado en el tenant de la organización.
- La app registra en Azure debe estar configurada con este tenant.
- El cliente SPA y la API deben estar correctamente autorizados.
- Si el usuario no pertenece al tenant, MSAL rechazará el login.

### Datos clave del tenant
```

```text
https://login.microsoftonline.com/49551105-7651-4748-a01b-22b28daeb087
```

## 7. Flujo normal de uso

1. Levantar el frontend local con `npm run dev`
2. Verificar que el backend y BFF estén corriendo en AWS EC2
3. Abrir la app en `http://localhost:5173`
4. Iniciar sesión con una cuenta del tenant
5. La app obtiene un access token con MSAL
6. El frontend llama a la API/BFF con el token en el header `Authorization: Bearer ...`
7. El backend valida el token y responde con clientes y pedidos

## 8. Comandos útiles

### Instalar dependencias

```bash
npm install
```

### Levantar frontend

```bash
npm run dev
```

### Build de producción

```bash
npm run build
```

### Previsualizar build

```bash
npm run preview
```

## 9. Solución de problemas comunes

### El login no funciona

- Verificar que el usuario pertenece al tenant correcto.
- Revisar que `VITE_ENTRA_TENANT_ID` y `VITE_SPA_CLIENT_ID` estén correctos.
- Revisar que la app registration en Azure tenga la redirección correcta.

### El frontend carga pero no trae datos

- Verificar que el BFF en AWS esté levantado.
- Revisar la URL base del BFF.
- Revisar logs del servicio EC2.
- Verificar que el token MSAL se está enviando en la request.

### El backend no responde

- Confirmar que la EC2 está encendida.
- Revisar proceso Java activo:

```bash
ps -ef | grep java
```

- Revisar logs:

```bash
tail -f /var/log/ms-clientes.log
tail -f /var/log/ms-pedidos.log
tail -f /var/log/bff.log
```

## 10. Recomendación final

Para trabajar en este proyecto:

- Frontend: correr localmente
- Backend: validar que EC2 esté encendido y accesible
- Login: usar usuarios del tenant configurado en Entra ID
- Si algo falla, revisar primero los logs del servicio y la configuración MSAL del tenant

