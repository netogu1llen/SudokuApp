# Sudoku App
#Ernesto Guillén Guerrero A01704967

Una aplicación moderna de Sudoku para Android, construida con Jetpack Compose, siguiendo el patrón de arquitectura MVVM y los principios de Clean Architecture.

## Características

- Generación de rompecabezas de Sudoku de diferentes tamaños (4x4 y 9x9)
- Tres niveles de dificultad (Fácil, Medio, Difícil)
- Tablero interactivo de Sudoku con validación de movimientos
- Funcionalidad de tomar notas para las celdas
- Guardar y continuar juegos en progreso
- Verificar la corrección de la solución
- Juego sin conexión después de generar el rompecabezas
- UI responsiva que se adapta a diferentes tamaños de pantalla

## Arquitectura

Esta aplicación sigue el enfoque de Clean Architecture con el patrón MVVM (Model-View-ViewModel):

### Capas de Clean Architecture

- **Capa de Datos**: Maneja las operaciones de datos con API remota y base de datos local
- **Capa de Dominio**: Contiene la lógica de negocios, modelos y casos de uso
- **Capa de Presentación**: Componentes de UI y ViewModels

### Componentes Clave

- **Modelo**: Modelos de dominio que representan los rompecabezas de Sudoku y el estado del juego
- **Vista**: Componentes UI de Jetpack Compose
- **ViewModel**: Gestiona el estado de la UI y la lógica de negocios
- **Repositorio**: Fuente única de verdad que coordina los datos desde la API y la base de datos local

## Tecnología

- **Kotlin**: Lenguaje de programación principal
- **Jetpack Compose**: Kit de herramientas moderno para construir UI nativas
- **Coroutines & Flow**: Para programación asíncrona
- **Hilt**: Inyección de dependencias
- **Room**: Base de datos local para la persistencia del juego
- **Retrofit**: Para solicitudes de red a la API de Sudoku
- **Navigation Compose**: Para la navegación entre pantallas
- **Material3**: Para la implementación del sistema de diseño


## Integración con la API

La aplicación se integra con la API Ninjas Sudoku API para generar los rompecabezas:

- Endpoint de la API: `https://api.api-ninjas.com/v1/sudoku`
- Parámetros:
  - `width`: Ancho del cuadro (2 para 4x4, 3 para 9x9)
  - `height`: Alto del cuadro (2 para 4x4, 3 para 9x9)
  - `difficulty`: Dificultad del rompecabezas (fácil, medio, difícil)

## Cómo Construir y Ejecutar

1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Ejecuta la aplicación en un emulador o dispositivo físico


## Licencia

Este proyecto es de código abierto y está disponible bajo la Licencia MIT.
