# 🐶 Age Dogs

**Age Dogs** es una aplicación móvil desarrollada para ofrecer una experiencia integral en el cuidado y bienestar de los perros. Permite gestionar mascotas, consultar información sobre su edad y etapa de vida, recibir recomendaciones mediante inteligencia artificial y acceder a servicios y productos para animales.

## ✨ Características principales

- 🐾 **Gestión de mascotas:** registro, edición, fotografías y consulta de información de cada perro.
- 🧮 **Calculadora de edad:** conversión de edad canina a edad humana.
- ❤️ **Etapas de vida:** recomendaciones de alimentación, salud, ejercicio y chequeos según edad y tamaño.
- 🤖 **Asistente IA:** consultas y orientación personalizada sobre el cuidado de las mascotas.
- 🏥 **Veterinarias cercanas:** búsqueda de clínicas veterinarias mediante ubicación y mapas.
- 🛒 **Marketplace:** catálogo de alimentos, productos, inventario y carrito de compras.
- 💳 **Pedidos y pagos:** gestión de pedidos e integración de flujo de pagos con Wompi.
- 💬 **Chat:** comunicación entre usuarios.
- 🔐 **Autenticación:** registro, inicio de sesión, recuperación de contraseña y perfil de usuario.

## 🛠️ Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje principal |
| Android Studio | Entorno de desarrollo |
| Jetpack Compose | Interfaces de usuario |
| Material Design 3 | Diseño visual |
| Firebase Authentication | Autenticación |
| Cloud Firestore | Base de datos |
| Firebase Storage | Almacenamiento de imágenes |
| Google Maps / Places | Servicios de ubicación |
| Retrofit / OkHttp | Comunicación HTTP |
| Node.js / Express | Backend |
| Firebase Admin SDK | Servicios del backend |
| Wompi | Integración de pagos |

## 🏗️ Arquitectura

El proyecto utiliza una organización por capas para separar responsabilidades y facilitar el mantenimiento:

- **UI:** pantallas y componentes desarrollados con Jetpack Compose.
- **ViewModels:** gestión del estado y lógica de presentación.
- **Data:** modelos, repositorios y conexión con Firebase y servicios externos.
- **Domain:** lógica de negocio, cálculos de edad y clasificación de etapas de vida.
- **Backend:** API REST para operaciones de pedidos, pagos y comunicación con servicios de IA.

## 🚀 Instalación

1. Clona el repositorio:

   ```bash
   git clone https://github.com/Cristianm23/Age_Dogs.git
   ```

2. Abre el proyecto desde Android Studio.

3. Configura Firebase y agrega tu archivo `google-services.json` en el módulo `app`.

4. Configura las credenciales y variables de entorno necesarias para los servicios externos.

5. Sincroniza Gradle y ejecuta la aplicación desde Android Studio.

### Backend

Para ejecutar el servidor:

```bash
cd backend
npm install
```

Configura las variables de entorno tomando como referencia `backend/.env.example` y ejecuta:

```bash
npm start
```

## 🎨 Diseño

Age Dogs utiliza una identidad visual basada en tonos cálidos, colores tierra y componentes Material Design 3, buscando transmitir confianza, cercanía y bienestar animal.

## 👨‍💻 Autores

**Cristian Muñoz Y Juan Felipe Higuera**

Desarrollador de software | Android · Kotlin · Firebase

---

*Age Dogs — Tecnología al servicio del bienestar de nuestras mascotas. 🐾*
