# Guía de Imágenes para Entidades

## Ubicación
Coloca las imágenes PNG de las entidades en la carpeta:
```
src/main/resources/images/
```

## Nombres de Archivo
Los nombres de archivo deben coincidir exactamente con el tipo de entidad en **minúsculas**:

- `granos_de_cebada.png` - Para la entidad GRANOS_DE_CEBADA
- `lupulo.png` - Para la entidad LUPULO
- `levadura.png` - Para la entidad LEVADURA
- `mosto.png` - Para la entidad MOSTO
- `cerveza.png` - Para la entidad CERVEZA
- `botella_con_cerveza.png` - Para la entidad BOTELLA_CON_CERVEZA
- `caja_vacia.png` - Para la entidad CAJA_VACIA
- `caja_con_cervezas.png` - Para la entidad CAJA_CON_CERVEZAS

## Características
- **Formato**: PNG con transparencia (recomendado)
- **Tamaño**: Aproximadamente 32x32 píxeles o similar (se escalará automáticamente)
- **Orientación**: La imagen debe "mirar" hacia la derecha (0 grados) para que la rotación funcione correctamente

## Comportamiento
- Si la imagen existe, se renderiza **en lugar** de la forma circular con emoji
- Si la imagen NO existe, se usa el **renderizado por defecto** (círculo con color e icono)
- Las imágenes rotan automáticamente según la dirección del movimiento
- La etiqueta de texto (nombre corto) se muestra debajo de la imagen para identificación

## Ejemplo
Para añadir una imagen de cebada:
1. Crea o descarga una imagen PNG de granos de cebada
2. Guárdala como `granos_de_cebada.png`
3. Colócala en `src/main/resources/images/`
4. Recompila y ejecuta la simulación
5. ¡Las entidades GRANOS_DE_CEBADA ahora usarán tu imagen!

## Nota
No es necesario proporcionar imágenes para todas las entidades. El sistema funciona perfectamente con una mezcla de imágenes y formas por defecto.
