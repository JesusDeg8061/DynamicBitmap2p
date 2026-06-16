# DynamicBitmap Network

### Plataforma de almacenamiento distribuido P2P con redistribución inteligente de datos

---

## Descripción

**DynamicBitmap Network** es una plataforma de almacenamiento distribuido Peer-to-Peer (P2P) desarrollada en Java que permite fragmentar archivos en múltiples chunks, distribuirlos automáticamente entre nodos de la red y reconstruirlos dinámicamente bajo demanda.

El objetivo del proyecto es crear una red de almacenamiento descentralizada capaz de aprovechar el espacio disponible de múltiples equipos para compartir y almacenar información de forma distribuida, resiliente y escalable.

---

## Características principales

### Almacenamiento distribuido

* Fragmentación automática de archivos en chunks.
* Distribución dinámica de chunks entre nodos.
* Reconstrucción automática de archivos completos.
* Persistencia local de chunks en disco.

### Redistribución inteligente

* Balanceo automático de almacenamiento.
* Redistribución cuando nuevos nodos se conectan.
* Recuperación automática de chunks cuando nodos abandonan la red.
* Monitoreo de chunks pendientes de movimiento.

### Red P2P

* Comunicación entre nodos mediante sockets TCP.
* Descubrimiento de peers mediante Bootstrap Server.
* Relay Server para soporte de conectividad.
* Sincronización automática de metadatos y bitmaps.

### Bitmap distribuido

* Cada nodo mantiene un mapa de chunks disponibles.
* Intercambio eficiente de disponibilidad.
* Descarga únicamente de chunks faltantes.

### Persistencia

* Almacenamiento permanente de chunks.
* Recuperación automática después de reiniciar la aplicación.
* Sincronización automática al reconectarse a la red.

### Interfaz gráfica moderna

* Dashboard en tiempo real.
* Gestión de archivos compartidos.
* Gestión de contactos y nodos.
* Estadísticas de almacenamiento.
* Tema claro y tema Matrix.
* Preferencias persistentes.

### Gestión de identidad

* Generación automática de NodeID único.
* Administración de contactos mediante identificadores de nodo.
* Preparación para comunicación cifrada entre peers.

---

## Dashboard

El sistema incluye un panel administrativo en tiempo real que muestra:

* Nodos conectados.
* Archivos compartidos.
* Almacenamiento utilizado.
* Espacio distribuido.
* Chunks pendientes de redistribución.

---

## Arquitectura

```text
Usuario (JavaFX UI)
        ↓
Dashboard / Archivos / Red
        ↓
Node
        ↓
Bitmap Distribuido
        ↓
Chunk Storage
        ↓
Red P2P (TCP)
        ↓
Bootstrap + Relay
```

### Componentes principales

| Componente      | Función                             |
| --------------- | ----------------------------------- |
| Node            | Gestión de chunks y sincronización  |
| DynamicBitmap   | Control de disponibilidad de chunks |
| ChunkStorage    | Persistencia local                  |
| FileChunker     | División de archivos                |
| FileAssembler   | Reconstrucción de archivos          |
| NodeServer      | Comunicación entre nodos            |
| BootstrapClient | Descubrimiento de peers             |
| RelayClient     | Comunicación mediante relay         |
| ContactManager  | Gestión de contactos                |
| ThemeManager    | Gestión de temas                    |
| JavaFX UI       | Interfaz de usuario                 |

---

## Flujo de funcionamiento

1. Usuario comparte un archivo.
2. El archivo se divide en chunks.
3. Los chunks se distribuyen entre nodos.
4. Cada nodo almacena únicamente la porción que le corresponde.
5. Nuevos nodos reciben automáticamente parte de los datos.
6. Si un nodo abandona la red, los chunks son redistribuidos.
7. El archivo puede reconstruirse bajo demanda desde múltiples nodos.

---

## Características técnicas

### Sistemas distribuidos

* Fragmentación distribuida.
* Balanceo dinámico.
* Redistribución automática.
* Sincronización entre nodos.

### Concurrencia

* Multithreading.
* Sincronización de procesos de red.
* Transferencia paralela de chunks.

### Redes

* TCP/IP.
* Peer Discovery.
* Bootstrap Server.
* Relay Server.

---

## Seguridad

Actualmente incluye:

* Hashing SHA-256 para verificación de integridad.
* Identificadores únicos por nodo.

En desarrollo:

* Cifrado AES para chunks.
* Mensajería segura entre contactos.
* Intercambio seguro de claves.

---

## Tecnologías utilizadas

* Java 21
* JavaFX
* TCP/IP Sockets
* Multithreading
* Oracle Cloud Infrastructure (OCI)
* WiX Toolset
* JPackage
* Git
* Programación Orientada a Objetos (POO)

---

## Estado del proyecto

```text
✔ Compartición de archivos
✔ Distribución de chunks
✔ Persistencia local
✔ Redistribución automática
✔ Dashboard JavaFX
✔ Gestión de contactos
✔ Tema Matrix
✔ Instalador Windows (.exe)

🚧 Mensajería P2P
🚧 Cifrado AES
🚧 Eliminación completa de dependencias relay
🚧 Replicación avanzada
```

---

## Instalación

### Windows

Ejecutar:

```text
DynamicBitmap-1.0.exe
```

El instalador incluye:

* Runtime Java integrado
* Configuración automática
* Acceso directo
* Menú Inicio

No se requiere instalación previa de Java.

---

## Autor

Jesus Alberto Degollado Lopez

---

## Licencia

Proyecto experimental y educativo.

---

⭐ Si te interesa el almacenamiento distribuido, los sistemas P2P y las arquitecturas descentralizadas, no olvides dejar una estrella en GitHub.
