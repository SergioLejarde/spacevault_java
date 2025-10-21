# 🚀 Guía de Ejecución del Sistema Distribuido **SpaceVault**

Este documento explica **qué comandos ejecutar, en qué orden y por qué**, para levantar el sistema distribuido **SpaceVault** completo.  
Incluye: compilación, servidores de base de datos (TCP), nodos de almacenamiento (RMI), servidor SOAP, cliente y verificación en PostgreSQL.

> 🧩 **Requisitos previos:**  
> - Java y Maven instalados  
> - PostgreSQL configurado con las bases `spacevaultjava` y `spacevaultjava_replica`  
> - Carpetas `data-Node1/` y `data-Node2/` creadas en la raíz del proyecto  

---

## 🧱 0. Preparación (Compilar el proyecto)

```bash
mvn clean compile
```

🔹 **Propósito:** limpia compilaciones previas y recompila todas las clases Java en `target/classes`.  
Asegura que el proyecto esté actualizado antes de iniciar los servicios.

---

## 🗄️ 1. Servidores de Base de Datos (TCP + PostgreSQL)

Estos servidores manejan la comunicación **TCP** y la replicación lógica entre la base de datos principal y su réplica.

### 🧩 1.1. Levantar la réplica primero (puerto TCP 9100)

```bash
mvn exec:java -Dexec.mainClass="com.spacevault.servidor.db.DBServerReplica"
```

🔹 **Por qué primero:** el cliente DB reenviará cada comando al principal y a la réplica; si la réplica no está arriba, se generará `Connection refused`.

---

### 💾 1.2. Levantar el servidor principal (puerto TCP 9090)

```bash
mvn exec:java -Dexec.mainClass="com.spacevault.servidor.db.DBServer"
```

🔹 **Propósito:** punto de verdad principal del sistema.  
Ambos servidores crean las tablas necesarias si no existen.

🟢 **Validación esperada:**
```
Conectado a PostgreSQL...
🗄️ DBServer escuchando en TCP 9090
```

---

## 🛰️ 2. Nodos de Almacenamiento (RMI)

Los nodos almacenan archivos de forma redundante.  
Cada uno escucha peticiones del servidor SOAP a través de **Java RMI**.

### 📦 2.1. Nodo 1 (RMI 1099)

```bash
mvn exec:java -Dexec.mainClass="com.spacevault.nodos.NodeServer"
```

🔹 **Función:** recibe y guarda archivos o directorios enviados por el servidor SOAP.  
Almacena su información bajo `data-Node1/`.

---

### 📦 2.2. Nodo 2 (RMI 1100)

```bash
mvn exec:java -Dexec.mainClass="com.spacevault.nodos.NodeServer2"
```

🔹 **Función:** provee redundancia y respaldo.  
Cualquier operación (crear, subir, eliminar) se replica entre ambos nodos.

🟢 **Logs esperados:**
```
📁 NodoX creó: usuario/ruta/
💾 NodoX guardó archivo: usuario/ruta/archivo.txt
```

---

## ☁️ 3. Servidor de Aplicación (SOAP)

Publica el servicio web en **http://localhost:8080/spacevault**

```bash
mvn exec:java -Dexec.mainClass="com.spacevault.servidor.ServerPublisher"
```

🔹 **Propósito:** coordina toda la lógica del sistema.  
Recibe peticiones del cliente, las distribuye a los nodos (RMI) y comunica los cambios a la BD principal y réplica (TCP).

🟢 **Logs esperados:**
```
📤 Enviando a puerto 9090: INSERT INTO usuarios...
📤 Enviando a puerto 9100: INSERT INTO usuarios...
📥 Respuesta desde puerto 9090: OK
```

---

## 💻 4. Cliente (Interfaz SOAP en consola)

Ejecuta el cliente que permite interactuar con el sistema.

```bash
mvn exec:java -Dexec.mainClass="com.spacevault.cliente.ClienteSOAP"
```

🔹 **Funciones disponibles:**
- Registro e inicio de sesión  
- Creación de directorios  
- Subida y descarga de archivos  
- Eliminación, renombrado, y compartición  
- Consulta de estado y estructura de directorios  

🟢 **Validación esperada:** cada acción en el cliente genera logs en DBServer, DBReplica y en los nodos.

---

## 🧪 5. Verificación en PostgreSQL

Abre dos terminales separadas y ejecuta:

### 🗃️ 5.1. Conectar a la base principal
```bash
psql spacevaultjava
```

### 🗃️ 5.2. Conectar a la base réplica
```bash
psql spacevaultjava_replica
```

Luego ejecuta los siguientes comandos dentro de cada sesión:

```sql
\dt
SELECT * FROM usuarios;
SELECT * FROM directorios;
SELECT * FROM archivos;
SELECT * FROM compartidos;
\q
```

🔹 **Propósito:** verificar la replicación de los datos (ambas bases deben mostrar la misma información).

---

## 🧹 6. Reiniciar o limpiar los nodos (opcional)

Si deseas reiniciar el entorno o borrar los archivos replicados:

```bash
rm -rf data-Node1/*
rm -rf data-Node2/*
```

⚠️ **Advertencia:** esto elimina todos los archivos almacenados en los nodos.

---

## 🧭 7. Orden recomendado

| Paso | Servicio | Puerto / Ruta | Comando |
|------|-----------|----------------|----------|
| 1 | **DB Réplica** | TCP 9100 | `mvn exec:java -Dexec.mainClass="com.spacevault.servidor.db.DBServerReplica"` |
| 2 | **DB Principal** | TCP 9090 | `mvn exec:java -Dexec.mainClass="com.spacevault.servidor.db.DBServer"` |
| 3 | **Nodo 1** | RMI 1099 | `mvn exec:java -Dexec.mainClass="com.spacevault.nodos.NodeServer"` |
| 4 | **Nodo 2** | RMI 1100 | `mvn exec:java -Dexec.mainClass="com.spacevault.nodos.NodeServer2"` |
| 5 | **Servidor SOAP** | HTTP 8080 | `mvn exec:java -Dexec.mainClass="com.spacevault.servidor.ServerPublisher"` |
| 6 | **Cliente SOAP** | CLI | `mvn exec:java -Dexec.mainClass="com.spacevault.cliente.ClienteSOAP"` |

---

## 🧰 Diagnóstico de errores comunes

| Problema | Causa probable | Solución |
|-----------|----------------|-----------|
| `Connection refused` en 9100 | No se inició DBServerReplica | Inícialo antes que el principal |
| `NumberFormatException` en ClienteSOAP | Opción vacía en menú | Vuelve a ejecutar el cliente y elige una opción válida |
| Nodos no guardan archivos | No se ejecutó NodeServer/NodeServer2 | Verifica que ambos estén corriendo |
| Tablas vacías | No se enviaron operaciones desde el cliente | Realiza alguna acción (crear usuario, subir archivo) |

---

## 🔄 (Opcional) Limpieza completa y recompilación

```bash
mvn clean compile
```

🔹 **Propósito:** reinicia el proyecto y recompila el código después de cambios o errores.

---

## 📚 Conclusión

Con esta secuencia se demuestra que el sistema **SpaceVault** cumple las características de un **sistema distribuido**:

- Comunicación mediante **SOAP** entre cliente y servidor  
- Comunicación **RMI** entre el servidor y los nodos  
- Comunicación **TCP** entre el servidor y las bases de datos  
- **Replicación y redundancia** de datos y archivos  
- **Tolerancia a fallos parcial** (independencia entre nodos y bases)  
- **Concurrencia** en las operaciones distribuidas

---
