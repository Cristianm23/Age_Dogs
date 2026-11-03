# Reglas de Seguridad de Firebase Storage — Age Dogs

Para permitir que los usuarios autenticados suban y descarguen las fotos de sus perros en Firebase Storage sin recibir errores de permisos (`PERMISSION_DENIED`), publica las siguientes reglas en la consola de Firebase (**Storage > Reglas**):

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /perros/{userId}/{allPaths=**} {
      // Permite leer y escribir archivos en la carpeta del usuario autenticado
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```
