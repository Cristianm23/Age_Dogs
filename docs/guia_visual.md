# Guía Visual y Sistema de Diseño — Age Dogs

## 1. Identidad Visual y Concepto
**Age Dogs** es una plataforma de servicios integrales para perros. La línea gráfica transmite calidez, confianza y afecto canino mediante tonos tierra y acentos contrastantes.

---

## 2. Paleta de Colores (Material Design 3)

### Paleta Principal (Café Base `#5D4037`)
- **Primary (Light)**: `#5D4037` | **OnPrimary**: `#FFFFFF`
- **Primary Container (Light)**: `#D7CCC8` | **OnPrimary Container**: `#3E2723`
- **Primary (Dark)**: `#D7CCC8` | **OnPrimary**: `#3E2723`
- **Primary Container (Dark)**: `#4E342E` | **OnPrimary Container**: `#D7CCC8`

### Paleta Secundaria (Amarillo Arenoso Base `#E6C27A`)
- **Secondary (Light)**: `#8C6E1E` | **OnSecondary**: `#FFFFFF`
- **Secondary Container (Light)**: `#F3D698` | **OnSecondary Container**: `#2A1E00`
- **Secondary (Dark)**: `#E6C27A` | **OnSecondary**: `#422C00`
- **Secondary Container (Dark)**: `#5E4200` | **OnSecondary Container**: `#F3D698`

### Paleta Terciaria — Acentos Críticos (Rojo Ferrari Base `#D40000`)
- **Tertiary (Light)**: `#D40000` | **OnTertiary**: `#FFFFFF`
- **Tertiary Container (Light)**: `#FFDAD6` | **OnTertiary Container**: `#410002`
- **Tertiary (Dark)**: `#FFB4AB` | **OnTertiary**: `#690005`
- **Tertiary Container (Dark)**: `#93000A` | **OnTertiary Container**: `#FFDAD6`
> *Uso del Terciario*: Reservado para estados de emergencia médica, eliminación de registros, alertas críticas y errores graves.

### Fondos y Superficies (Cálidas / Crema)
- **Background (Light)**: `#FAF6F0` (Tono crema cálido) | **OnBackground**: `#201A18`
- **Surface (Light)**: `#FFFDF8` | **OnSurface**: `#201A18`
- **Surface Variant (Light)**: `#F5ECE5` | **OnSurface Variant**: `#52443D`
- **Background (Dark)**: `#1A1210` | **OnBackground**: `#EDE0D9`
- **Surface (Dark)**: `#1E1614` | **OnSurface**: `#EDE0D9`

---

## 3. Tipografía
Se utiliza la tipografía predeterminada del sistema (`FontFamily.Default` / Roboto / Sans-serif).
**Justificación Técnica**:
1. **Rendimiento**: Evita tiempos de carga de archivos de fuentes externos (.ttf / .otf).
2. **Confiabilidad Offline**: Garantiza renderizado instantáneo sin dependencia de red.
3. **Escalabilidad y Accesibilidad**: Soporta automáticamente el escalado dinámico de texto (Dynamic Type) y configuraciones de accesibilidad del usuario en el sistema operativo.

---

## 4. Componentes Reutilizables

| Componente | Función / Descripción | Ubicación en Código |
| :--- | :--- | :--- |
| `AppPrimaryButton` | Botón con fondo primario café, estado de carga integrado y 50dp de altura. | `ui/components/Components.kt` |
| `AppSecondaryButton` | Botón secundario contorneado para acciones secundarias. | `ui/components/Components.kt` |
| `AppDangerButton` | Botón de peligro/alerta con fondo rojo Ferrari `#D40000`. | `ui/components/Components.kt` |
| `AppTextField` | Campo de texto M3 con soporte para etiquetas, errores e íconos. | `ui/components/Components.kt` |
| `AppCard` | Tarjeta contenedora con elevación sutil de 2dp y superficie variante. | `ui/components/Components.kt` |
| `AppTopBar` | Barra de navegación superior con color primario. | `ui/components/Components.kt` |
| `AppLoadingState` | Indicador circular con mensaje de carga centrado. | `ui/components/Components.kt` |
| `AppEmptyState` | Estado informativo libre de contenido con ícono descriptivo. | `ui/components/Components.kt` |
| `AppErrorState` | Pantalla/sección de error con opción de reintento. | `ui/components/Components.kt` |

---

## 5. Logotipo e Íconos Adaptativos
- **Logotipo Principal**: `res/drawable/ic_logo_dog.xml` — Medallón circular con huella canina en amarillo arenoso y corazón central en rojo Ferrari.
- **Logotipo Monocromático**: `res/drawable/ic_logo_monochrome.xml`.
- **Ícono Adaptativo**: Configurado en `res/mipmap-anydpi-v26/ic_launcher.xml` con capa de fondo café, capa frontal escalada y capa monocromática para Android 13+.
