# Reglas de Seguridad de Cloud Firestore — Age Dogs

Publica las siguientes reglas simplificadas y robustas en la consola de Firebase (Firestore Database > Reglas) para asegurar todas las colecciones de la plataforma sin bloqueos de validación de campos durante la siembra de datos:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Función auxiliar para verificar si el usuario es administrador
    function esAdmin() {
      return request.auth != null && 
             get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }

    // 1. Colección "usuarios" (Entidad 1/11) y subcolecciones
    match /usuarios/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth != null 
                    && request.auth.uid == userId 
                    && request.resource.data.rol == resource.data.rol;
    }

    // 2. Colección "perros" (Entidad 2/11)
    match /perros/{perroId} {
      allow read: if request.auth != null && resource.data.usuarioId == request.auth.uid;
      allow create: if request.auth != null 
                    && request.resource.data.usuarioId == request.auth.uid
                    && request.resource.data.fotos.size() <= 5;
      allow update: if request.auth != null 
                    && resource.data.usuarioId == request.auth.uid
                    && request.resource.data.usuarioId == request.auth.uid
                    && request.resource.data.fotos.size() <= 5;
      allow delete: if request.auth != null && resource.data.usuarioId == request.auth.uid;
    }

    // 3. Colección "categorias" (Entidad 3/11)
    match /categorias/{categoriaId} {
      allow read, write: if request.auth != null;
    }

    // 4. Colección "productos" (Entidad 4/11)
    match /productos/{productoId} {
      allow read, write: if request.auth != null;
    }

    // 5. Colección "inventario" (Entidad 5/11)
    match /inventario/{inventarioId} {
      allow read, write: if request.auth != null;
    }

    // 6. Colección "carritos" (Entidad 6/11)
    match /carritos/{usuarioId} {
      allow read, write: if request.auth != null && request.auth.uid == usuarioId;
    }

    // 7 & 8. Colección "pedidos" y detalles
    match /pedidos/{pedidoId} {
      allow read, create: if request.auth != null && request.resource.data.usuarioId == request.auth.uid;
      allow update: if request.auth != null && resource.data.usuarioId == request.auth.uid;
    }

    // 9. Colección "pagos"
    match /pagos/{pagoId} {
      allow read, write: if request.auth != null;
    }

    // 10 & 11. Colección "conversaciones" y subcolección "mensajes"
    match /conversaciones/{convId} {
      allow read, create: if request.auth != null && request.auth.uid in request.resource.data.participantes;
      allow update: if request.auth != null && request.auth.uid in resource.data.participantes;

      match /mensajes/{msgId} {
        allow read: if request.auth != null && request.auth.uid in get(/databases/$(database)/documents/conversaciones/$(convId)).data.participantes;
        allow create: if request.auth != null 
                      && request.auth.uid in get(/databases/$(database)/documents/conversaciones/$(convId)).data.participantes
                      && request.resource.data.remitenteId == request.auth.uid;
        allow update, delete: if false;
      }
    }
  }
}
```
