# Google Login Setup

## Google Cloud Console

1. Crea un OAuth Client ID de tipo **Web application**.
2. En **Authorized JavaScript origins** agrega:
   - `http://localhost:4200`
3. En **Authorized redirect URIs** agrega:
   - `http://localhost:4200`
4. Copia el **Client ID** generado.

## Frontend

Pega el Client ID en:

- `frontend/src/environments/environment.ts`
- `frontend/src/environments/environment.prod.ts`

```ts
googleClientId: 'TU_CLIENT_ID.apps.googleusercontent.com'
```

## Backend

Configura la variable de entorno `GOOGLE_CLIENT_ID` con el mismo Client ID:

```powershell
$env:GOOGLE_CLIENT_ID="TU_CLIENT_ID.apps.googleusercontent.com"
```

El endpoint real usado por Angular es:

```http
POST /api/auth/google
```

El backend valida el `idToken` con Google, confirma que el `aud` coincida con `GOOGLE_CLIENT_ID`, exige `email_verified=true` y crea o reutiliza el usuario por correo.
