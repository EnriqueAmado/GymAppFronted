[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/EnriqueAmado/GymAppFronted)
GymApp - Sistema de Seguimiento de Entrenamientos

¡Bienvenido a **GymApp**! Una solución de software integral diseñada para atletas y entrenadores que buscan registrar, estructurar y analizar la evolución de sus marcas en el gimnasio de forma analítica y eficiente.

Este proyecto representa el MVP (Producto Mínimo Viable) para la entrega final, comunicando de forma nativa una aplicación móvil Android con un potente servidor backend de microservicios.

---

## 🚀 Características Principales

* **Autenticación Segura:** Registro e inicio de sesión de usuarios mediante tokens dinámicos.
* **Gestión de Rutinas:** Creación y personalización de entrenamientos añadiendo ejercicios específicos de una base de datos optimizada.
* **Historial de Cargas:** Registro detallado de series, volumen de entrenamiento (Kilos x Repeticiones) y fechas.
* **Gráfica de Progreso Interactiva (Core UX):** Visualización avanzada del progreso de fuerza mediante nodos interactivos en pantalla. Al pulsar sobre cualquier punto de la gráfica, se despliega un marcador flotante (*MarkerView*) con el desglose exacto de la carga y la fecha sin saturar la interfaz.

---

## 🛠️ Tecnologías Utilizadas

El ecosistema de GymApp divide sus responsabilidades en dos capas de software independientes:

### 📱 Cliente Móvil (Frontend)
* **Entorno:** Android Studio
* **Lenguaje:** Java (SDK Nativo)
* **Arquitectura:** Patrón Facade y desacoplamiento de red.
* **Conectividad:** Retrofit para peticiones HTTP y serialización de JSON.
* **Gráficas:** MPAndroidChart (personalizada con componentes interactivos propios).

### 💻 Servidor (Backend API)
* **Entorno:** Django Web Framework & Django REST Framework
* **Lenguaje:** Python 3.14
* **Base de Datos:** SQLite3 (Estructura relacional optimizada con ORM)
* **Seguridad:** Django Cors Headers para intercambio seguro de recursos de origen cruzado (CORS).

---

## 🔧 Instalación y Puesta en Marcha Técnica

Sigue estos pasos para replicar el entorno de desarrollo localmente en cualquier máquina:

### 1. Preparación del Backend
Accede a la carpeta del backend, activa el entorno virtual e instala los requerimientos:
```bash
cd GymAppBackend
# Activar entorno virtual (.venv) en Windows
.venv\Scripts\activate

# Instalar dependencias esenciales
pip install django django-cors-headers djangorestframework
