# ElectroLab — Simulador de electricidad y magnetismo

Aplicación educativa de escritorio en español. Incluye cinco simuladores interactivos con gráficas, diagramas que responden a los controles, desarrollo de fórmulas y retos breves de comprensión.

## Abrir en IntelliJ IDEA

1. Descomprime la carpeta del proyecto si recibiste el archivo ZIP.
2. En IntelliJ IDEA, elige **File → Open** y selecciona la carpeta `SimuladorElectromagnetismo` (o su archivo `pom.xml`).
3. Configura un JDK 17 o posterior cuando IntelliJ lo solicite.
4. Abre `src/main/java/pe/educacion/electrolab/Main.java` y ejecuta `Main`.

El proyecto usa Java Swing y no necesita librerías externas.

## Simuladores

1. **Cargas y campo eléctrico** — fuerza de Coulomb, campo y potencial de una carga puntual, signo de la interacción y sentido de los vectores.
2. **Capacitancia y dieléctricos** — capacitor de placas paralelas; compara la inserción de un dieléctrico con el capacitor conectado a una batería o aislado.
3. **Corriente, resistencia y fem** — circuito con fuente, resistencia de carga y resistencia interna; muestra corriente, voltaje terminal, carga transferida y potencia.
4. **Circuitos de corriente directa** — redes de tres resistores en serie, paralelo o una combinación mixta; calcula corrientes, caídas de voltaje y resistencia equivalente.
5. **Campo magnético y fuerzas magnéticas** — fuerza sobre una partícula cargada o un conductor con corriente; explora el ángulo entre el movimiento/corriente y el campo.

## Interacciones para el estudiante

- Ajusta variables con campos de texto o deslizadores; los resultados y las gráficas se actualizan en tiempo real.
- Arrastra la carga de prueba q₂ en el esquema electrostático para cambiar la distancia. En el capacitor de placas paralelas, arrastra la placa derecha para variar la separación.
- Agrega una segunda fuente q₃ en el simulador electrostático y observa la superposición del campo neto.
- En capacitores, cambia entre placas con dieléctrico y un banco de dos componentes en serie o paralelo.
- En magnetismo, observa los vectores en una proyección 3D y cambia θ para explorar la componente perpendicular.
- Abre **Ver desarrollo paso a paso** para revisar sustituciones y unidades. Usa **Reto** para responder una pregunta conceptual con retroalimentación.
- **Guardar** y **Cargar** conservan las entradas en un archivo `.properties`; **Exportar CSV** guarda entradas, resultados y puntos de la gráfica. **Informe PNG** crea una imagen con el resultado, el esquema y la gráfica para el informe del curso.

## Relaciones usadas

- Ley de Coulomb: `F = k |q₁ q₂| / r²`; campo de una carga puntual: `E = k |q| / r²`; potencial: `V = k q / r`.
- Placas paralelas: `C₀ = ε₀ A / d`; con dieléctrico: `C = κ C₀`; carga: `Q = C V`; energía: `U = ½ C V²`.
- Fuente con resistencia interna: `I = ε / (R + r)` y `Vterminal = ε − I r`.
- Red resistiva: `V = I R`; serie: `Req = ΣR`; paralelo: `1/Req = Σ(1/R)`.
- Fuerza magnética: `F = |q| v B sen(θ)` o `F = I ℓ B sen(θ)` para un tramo recto de conductor.

Los valores se convierten a unidades SI antes del cálculo. Los dibujos son esquemas didácticos y no están a escala. Se usan aproximaciones de campo uniforme para placas paralelas y se omiten efectos de borde, variaciones térmicas y componentes parásitas.

## Fuentes de contenido

Los temas y el vocabulario siguen las lecturas proporcionadas para carga eléctrica y campo eléctrico (capítulo 21), capacitancia y dieléctricos (capítulo 24), corriente/resistencia/fuerza electromotriz (capítulo 25), circuitos de corriente directa (capítulo 26), y campo magnético/fuerzas magnéticas (capítulo 27). Las explicaciones de la interfaz están redactadas para este simulador.
