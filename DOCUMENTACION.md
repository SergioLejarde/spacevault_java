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

En SpaceVault, uso TCP en la parte de la comunicación entre el Servidor SOAP y los Servidores de Base de Datos (DBServer y DBServerReplica).

Cada vez que el servidor central necesita guardar o consultar algo (por ejemplo: un usuario nuevo o un archivo),
TCP se usa para enviar comandos SQL como texto a través de una conexión confiable.

Ese mensaje viaja mediante TCP desde el ServerPublisher hasta el DBServer, y TCP garantiza que:

El mensaje llegue completo.

Los comandos se ejecuten en el mismo orden en que fueron enviados.

No haya duplicados ni pérdidas.

Por eso TCP garantiza:
✅ Confiabilidad (nunca se pierden mensajes)
✅ Orden (todo llega en el mismo orden en que fue enviado)
✅ Integridad (datos sin corrupción)
✅ Control de flujo (no se satura la red)

Uso TCP porque es el protocolo que garantiza una comunicación confiable entre procesos.
En el sistema SpaceVault, TCP se usa entre el servidor y las bases de datos, asegurando que los comandos SQL lleguen completos, en orden y sin pérdida.
Esto permite mantener sincronizadas la base principal y su réplica, incluso en un entorno distribuido con posibles fallos.”

### 🧩 1.1. Levantar la réplica primero (puerto TCP 9100)

```bash
mvn exec:java -Dexec.mainClass="com.spacevault.servidor.db.DBServerReplica"
```


🔹 **Por qué primero:** el cliente DB reenviará cada comando al principal y a la réplica; si la réplica no está arriba, se generará `Connection refused`. lo cual no es una falla del sistema, sino una respuesta esperada ante un nodo caído. lo que demuestra tolerancia a fallos ya que Aun así, SpaceVault continúa funcionando con el servidor principal, demostrando tolerancia a fallos.

---

### 💾 1.2. Levantar el servidor principal (puerto TCP 9090)

```bash
mvn exec:java -Dexec.mainClass="com.spacevault.servidor.db.DBServer"
```
Este comando le dice a Maven (la herramienta de construcción Java):

“Ejecuta la clase principal DBServer dentro del paquete com.spacevault.servidor.db.”

En otras palabras:
👉 estoy encendiendo el servidor de base de datos principal del sistema.

Es como si dentro de la nave espacial encendiera el módulo central de control de registros, el que guarda la bitácora principal de misiones (usuarios, archivos, directorios, etc.).

cuando ejecuto ese comando:

Se abre un Socket TCP en el puerto 9090 (esperando conexiones entrantes).

Se conecta a tu base de datos PostgreSQL principal (spacevaultjava).

Verifica si existen las tablas necesarias (usuarios, directorios, archivos, compartidos).

Si alguna falta, la crea automáticamente.

Así, al arrancar el sistema, la base siempre está lista para recibir operaciones, incluso si es la primera vez que la ejecutas.

🔹 **Propósito:** punto de verdad principal del sistema.  
Ambos servidores crean las tablas necesarias si no existen.

🟢 **Validación esperada:**
```
Conectado a PostgreSQL...
🗄️ DBServer escuchando en TCP 9090
```

Qué significa “puerto TCP 9090”

El puerto 9090 es como una puerta de comunicación en la computadora (o en la nave).
Ahí es donde el servidor se queda “escuchando” mensajes que llegan desde otros módulos.

En SpaceVault:

El ServerPublisher (servidor SOAP) actúa como el capitán que da órdenes.

Cuando necesita guardar algo (por ejemplo, un nuevo archivo o usuario),
envía un mensaje por TCP al puerto 9090 donde está el DBServer.

Ejemplo del mensaje que viaja por TCP:

INSERT INTO usuarios (nombre, clave) VALUES ('Sergio', '1234');


El DBServer lo recibe y lo ejecuta en PostgreSQL (la base principal).
Por eso se le llama servidor TCP, porque usa ese protocolo para comunicarse.

4. Por qué se llama “punto de verdad principal”

En un sistema distribuido con replicación, existe siempre una base principal y una o más réplicas.

La base principal (DBServer, puerto 9090) es el “punto de verdad”,
es decir, la fuente original y oficial de la información.
Toda escritura (INSERT, UPDATE, DELETE) pasa primero por ella.

La base réplica (DBServerReplica, puerto 9100) es un espejo:
recibe los mismos comandos para mantener una copia sincronizada.
Si el principal falla, la réplica tiene la información más reciente.

Por eso se llama punto de verdad principal, porque es la base que el sistema considera oficial y que manda las reglas de consistencia a las demás.

 Ejemplo dentro de la nave SpaceVault

Imagina que tu nave tiene una computadora principal y una copia de respaldo:

La principal (puerto 9090) recibe todas las órdenes del capitán.

La copia (puerto 9100) escucha las mismas órdenes, para poder continuar la misión si la principal se apaga.

Cuando enciendes el DBServer (9090) con ese comando, estás activando la consola de registros principal de la nave.

---

## 🛰️ 2. Nodos de Almacenamiento (RMI)

En tu sistema SpaceVault, los nodos son los módulos de la nave donde realmente se guardan los archivos físicos es decir a nivel del sistema de archivos del computador.

Los archivos se almacenan digitalmente de forma persistente en el sistema de archivos físico del equipo que ejecuta el nodo, Se pueden abrir desde el explorador de archivos,

Y si vas a data-Node1/ o data-Node2/, los verás allí con su contenido real.

Imagina que cada nodo es un almacén de datos digital dentro de la nave.
Cuando subes un archivo desde el cliente:

El servidor SOAP le dice al Nodo 1 y al Nodo 2 que guarden una copia.

Cada nodo crea una “caja” (una carpeta) dentro de su sistema de almacenamiento.

Esa “caja” contiene el archivo en formato binario —es digital, pero existe físicamente en el hardware de la nave.

Si uno de los nodos explota (falla), el otro sigue teniendo su copia,
porque la información estaba realmente escrita en su disco local.

Los nodos almacenan archivos de forma redundante.  
Cada uno escucha peticiones del servidor SOAP a través de **Java RMI**.

En SpaceVault, los nodos son los “almacenes de datos” de la nave espacial.
Cada nodo es un programa Java independiente (por ejemplo, NodeServer y NodeServer2) que:

Vive en su propio proceso (o incluso podría estar en otro computador).

Tiene su propia carpeta (data-Node1/, data-Node2/) donde guarda archivos.

Espera órdenes del centro de mando (Servidor SOAP).

👉 En otras palabras:
los nodos son los discos duros distribuidos de la nave, encargados de almacenar los archivos digitales de cada usuario.

“Almacenan archivos de forma redundante”

Redundancia significa que se guarda más de una copia de cada archivo en distintos nodos.
No para desperdiciar espacio, sino para proteger la información.

En SpaceVault:

Cuando subes un archivo desde el cliente, el Servidor SOAP lo envía a dos nodos distintos.

Ambos guardan el mismo archivo, cada uno en su carpeta local (data-Node1 y data-Node2).

📦 Ejemplo:
Si subes informe.pdf, el sistema hará:

Nodo 1 → data-Node1/sergio/informe.pdf
Nodo 2 → data-Node2/sergio/informe.pdf


Si el Nodo 1 se apaga, el Nodo 2 todavía tiene la copia.
Así tu sistema sigue funcionando: eso es tolerancia a fallos.


Escuchan peticiones del servidor SOAP”

Cada nodo no actúa por su cuenta, sino que espera órdenes del servidor principal (llamado ServerPublisher, el que publica el servicio SOAP).

Cuando el servidor SOAP necesita guardar, leer o borrar un archivo, no lo hace directamente:
le pide al nodo que lo haga por él.

Por ejemplo:

El cliente sube un archivo.

El servidor SOAP llama remotamente al nodo y ejecuta:

nodo1.guardarArchivo(usuario, archivo);
nodo2.guardarArchivo(usuario, archivo);


Los nodos están “escuchando” (esperando) esas llamadas constantemente.

Esto se logra gracias a Java RMI (Remote Method Invocation),
que permite que un objeto remoto (en otro proceso o computador) reciba llamadas como si fuera local.

ava RMI es una tecnología que permite invocar métodos en objetos que están en otra máquina.
Tu NodeServer implementa una interfaz remota llamada NodeRemote, con métodos como:

public interface NodeRemote extends Remote {
    void guardarArchivo(String usuario, byte[] archivo) throws RemoteException;
}


El servidor SOAP obtiene una referencia a ese nodo y puede ejecutar esos métodos a distancia,
por eso decimos que los nodos “escuchan peticiones RMI”.

💬 En lenguaje de red:

El nodo abre un puerto RMI (1099 o 1100).

El servidor se conecta y llama a los métodos remotos.

RMI se encarga de enviar los datos y ejecutar el código en el nodo correcto.


Analogía con la nave SpaceVault

Imagina que la nave tiene dos compartimentos de almacenamiento de datos:

Nodo 1 → “Almacén del ala izquierda”.

Nodo 2 → “Almacén del ala derecha”.

El centro de mando (servidor SOAP) no va físicamente hasta esos almacenes:
les manda una señal remota (RMI) que dice, por ejemplo:

“Guarden el archivo registro_mision.log en sus compartimentos.”

Ambos nodos ejecutan la orden al mismo tiempo y guardan una copia.
Así, aunque una parte de la nave se dañe, la otra conserva los datos.

Esa es la redundancia funcionando gracias a la comunicación remota (RMI).

. Qué significa “podría estar en otro computador”

El poder de Java RMI (Remote Method Invocation)
es que no le importa dónde esté el otro proceso.
Solo necesita una dirección IP y un puerto.

Entonces, si quisieras:

Podrías ejecutar el servidor SOAP en tu Mac.

Ejecutar el Nodo 1 en un portátil diferente (por ejemplo, el de un compañero).

Ejecutar el Nodo 2 en una tercera computadora.

Mientras estén conectados a la misma red Wi-Fi y uses la IP correcta,
el servidor SOAP podría seguir llamando a los métodos remotos de esos nodos.

📡 Por ejemplo:

rmi://192.168.1.12:1099/NodeServer


Eso significa:

“Conéctate al nodo que está en la IP 192.168.1.12, puerto 1099.”

💡 3. ¿Por qué eso es importante?

Porque es lo que convierte tu programa en un sistema distribuido real.

Un sistema no distribuido tiene todo en un solo lugar.
Pero un sistema distribuido reparte las tareas entre varios equipos, por ejemplo:

Un computador guarda archivos,

Otro coordina las operaciones,

Otro guarda los datos en base de datos.

Esto permite:

Escalar (agregar más nodos para más capacidad).

Mejorar tolerancia a fallos (si una máquina se apaga, las otras siguen).

Repartir la carga de trabajo (varias computadoras colaboran).

🚀 4. Ejemplo con tu nave SpaceVault

Piensa que tu nave tiene varias secciones:

El centro de mando (servidor SOAP) en el puente de control.

Nodo 1 en el ala izquierda.

Nodo 2 en el ala derecha.

Servidor de base de datos en el módulo de ingeniería.

Cada uno está en un lugar distinto físicamente,
pero todos están conectados por la red de la nave (como si fuera tu Wi-Fi).

El capitán (cliente SOAP) envía una orden → el centro de mando (servidor SOAP) la recibe →
y la retransmite a los módulos (nodos y base de datos) por la red.

Así trabajan en conjunto aunque no estén en la misma máquina.


### 📦 2.1. Nodo 1 (RMI 1099)

```bash
mvn exec:java -Dexec.mainClass="com.spacevault.nodos.NodeServer"
```

🔹 **Función:** recibe y guarda archivos o directorios enviados por el servidor SOAP.  
Almacena su información bajo `data-Node1/`.

Este comando le dice a Maven:

“Ejecuta la clase NodeServer del paquete com.spacevault.nodos.”

Esa clase es la que inicia el primer nodo de almacenamiento (Nodo 1).
Cuando lo haces, pasan estas cosas:

Java abre un puerto RMI (1099) para que otros programas puedan conectarse.

Registra un objeto remoto que puede ejecutar métodos a distancia, por ejemplo:

guardarArchivo()

leerArchivo()

borrarArchivo()

Se queda esperando peticiones del Servidor SOAP (el cerebro del sistema).
No hace nada hasta que el servidor le pida una acción.

Cuando el servidor SOAP le dice:
“Guarda este archivo de Sergio,”
el nodo crea una carpeta y guarda el archivo físicamente en:

data-Node1/

“El Nodo 1 es el primer módulo de almacenamiento del sistema SpaceVault.
Se ejecuta en el puerto RMI 1099 y se comunica con el servidor SOAP mediante Java RMI.
Su función es recibir las órdenes del servidor para guardar o leer archivos, los cuales se almacenan físicamente en la carpeta data-Node1/.
Esto garantiza que los archivos existan de forma persistente y puedan recuperarse incluso si otro nodo falla.”

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

El Servidor SOAP usa tanto TCP como RMI para comunicarse con los otros módulos,
pero no los recibe como cliente, sino que los usa como herramientas de comunicación.

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
