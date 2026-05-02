#  DynamicBitmap Network

### Sistema P2P descentralizado con distribución inteligente de archivos

---

##  Descripción

**DynamicBitmap Network** es una red **peer-to-peer (P2P) completamente descentralizada** que permite distribuir archivos entre múltiples nodos sin depender de un servidor central.

El sistema divide archivos en **chunks**, los distribuye en la red y los reconstruye dinámicamente cuando el usuario lo solicita, implementando un modelo inspirado en redes tipo BitTorrent pero con arquitectura propia.

---

##  Características principales

*  **Red descentralizada real**

  * Sin servidor central
  * Descubrimiento automático de nodos (Peer Discovery)

*  **Distribución por chunks**

  * División de archivos en fragmentos
  * Almacenamiento distribuido entre nodos

*  **Auto-replicación inteligente**

  * Sincronización automática entre nodos
  * Redistribución dinámica al entrar nuevos peers

*  **Reconstrucción de archivos**

  * Ensamblado completo desde múltiples nodos
  * Recuperación bajo demanda

*  **Bitmap distribuido**

  * Cada nodo mantiene estado de chunks
  * Intercambio eficiente de información

*  **Preparado para seguridad**

  * Integración con hash SHA-256 (base implementada)

---

##  Monitor de Red (Admin)

El sistema incluye un **monitor visual en tiempo real** para administración:

*  Nodos activos/inactivos
*  Conexiones dinámicas entre nodos
*  Chunks en movimiento (animación)
*  Velocidad por nodo (chunks/seg)
*  Contadores de envío/recepción
*  Visualización tipo red circular

---

##  Arquitectura

```
Usuario (MainUI)
        ↓
Nodo (Node)
        ↓
Red P2P (Sockets + Discovery)
        ↓
Distribución de chunks
        ↓
Monitor (NetworkMonitorUI)
```

### Componentes clave

| Componente           | Descripción                              |
| -------------------- | ---------------------------------------- |
| `Node`               | Manejo de chunks, replicación y descarga |
| `NodeServer`         | Comunicación entre nodos                 |
| `PeerDiscovery`      | Descubrimiento automático de peers       |
| `FileChunker`        | División de archivos                     |
| `FileAssembler`      | Reconstrucción                           |
| `NetworkEventSender` | Eventos hacia monitor                    |
| `NetworkMonitorUI`   | Visualización en tiempo real             |

---

##  Flujo del sistema

1. Usuario sube archivo
2. Se divide en chunks
3. Se distribuye entre nodos
4. Nuevos nodos se integran automáticamente
5. Se sincronizan chunks faltantes
6. Usuario puede reconstruir el archivo

---

##  Ejecución

### 1. Ejecutar nodos

```bash
Run MainUI
```

Puedes abrir múltiples instancias para simular red.

---

### 2. Ejecutar monitor (admin)

```bash
Run NetworkMonitorUI
```

---

## Innovación del proyecto

* ✔ Arquitectura 100% descentralizada
* ✔ Auto-sincronización sin intervención manual
* ✔ Visualización en tiempo real del tráfico
* ✔ Diseño modular extensible

---

## Limitaciones actuales

* No hay persistencia global de archivos (depende de nodos activos)
* Distribución no completamente balanceada (en mejora)
* No hay control de replicación máxima por chunk
* Seguridad básica (SHA-256 en proceso de integración completa)

---

##  Tecnologías utilizadas

* Java (Swing UI)
* Sockets (TCP/UDP)
* Multithreading
* Arquitectura P2P

---

##  Estado del proyecto

```
✔ Funcional
✔ Red distribuida activa
✔ Monitor en tiempo real
🚧 En evolución
```
##  Licencia

Uso libre para fines educativos y experimentales.

---

## Autor

Jesus Alberto Degollado Lopez
---

⭐ Si te gusta el proyecto, dale estrella en GitHub.
