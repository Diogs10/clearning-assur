# 🔐 Guide d'installation et configuration Keycloak pour Clearing API

## 📋 Table des matières
1. [Installation de Keycloak](#installation-de-keycloak)
2. [Configuration du Realm](#configuration-du-realm)
3. [Configuration du Client](#configuration-du-client)
4. [Création des Rôles](#création-des-rôles)
5. [Création des Utilisateurs](#création-des-utilisateurs)
6. [Test de l'authentification](#test-de-lauthentification)
7. [Intégration avec Angular](#intégration-avec-angular)

---

## 1. Installation de Keycloak

### Option A : Avec Docker (Recommandé)

```bash
# Lancer Keycloak
docker run -d \
  --name keycloak \
  -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:23.0.6 \
  start-dev
```

### Option B : Installation manuelle

1. Télécharger Keycloak depuis https://www.keycloak.org/downloads
2. Extraire l'archive
3. Lancer Keycloak :
   ```bash
   # Linux/Mac
   cd keycloak-23.0.6
   bin/kc.sh start-dev
   
   # Windows
   cd keycloak-23.0.6
   bin\kc.bat start-dev
   ```

### Accès à la console admin
- URL : http://localhost:8080
- Username : `admin`
- Password : `admin`

---

## 2. Configuration du Realm

### Créer un nouveau Realm

1. Cliquer sur **"master"** (en haut à gauche)
2. Cliquer sur **"Create Realm"**
3. Entrer le nom : `clearing-realm`
4. Cliquer sur **"Create"**

### Configurer les paramètres du Realm

1. Aller dans **Realm Settings**
2. Dans l'onglet **General** :
   - Display name : `Clearing Realm`
   - Enabled : `ON`
3. Dans l'onglet **Login** :
   - User registration : `ON` (optionnel)
   - Forgot password : `ON` (optionnel)
   - Remember me : `ON`
4. Dans l'onglet **Tokens** :
   - Access Token Lifespan : `5 Minutes` (ajuster selon besoin)
   - Refresh Token Lifespan : `30 Minutes`

---

## 3. Configuration du Client

### Créer le client API

1. Aller dans **Clients**
2. Cliquer sur **"Create client"**
3. Configuration :
   - **Client type** : `OpenID Connect`
   - **Client ID** : `clearing-api-client`
   - Cliquer sur **Next**

4. **Capability config** :
   - **Client authentication** : `ON`
   - **Authorization** : `OFF`
   - **Authentication flow** :
     - ✅ Standard flow
     - ✅ Direct access grants
     - ✅ Service accounts roles
   - Cliquer sur **Next**

5. **Login settings** :
   - **Root URL** : `http://localhost:8081`
   - **Valid redirect URIs** : 
     - `http://localhost:8081/*`
     - `http://localhost:4200/*` (pour Angular)
   - **Valid post logout redirect URIs** : 
     - `http://localhost:4200/*`
   - **Web origins** : 
     - `http://localhost:8081`
     - `http://localhost:4200`
   - Cliquer sur **Save**

### Récupérer le Client Secret

1. Aller dans l'onglet **Credentials**
2. Copier le **Client secret**
3. Le mettre dans `application.properties` :
   ```properties
   keycloak.credentials.secret=VOTRE_CLIENT_SECRET
   ```

---

## 4. Création des Rôles

### Créer les rôles Realm

1. Aller dans **Realm roles**
2. Cliquer sur **"Create role"**

#### Rôle USER
- **Role name** : `USER`
- **Description** : `Utilisateur standard`
- Cliquer sur **Save**

#### Rôle ADMIN
- **Role name** : `ADMIN`
- **Description** : `Administrateur avec tous les droits`
- Cliquer sur **Save**

#### Rôles supplémentaires (optionnels)
- `MANAGER` : Gestionnaire
- `ACCOUNTANT` : Comptable
- `AUDITOR` : Auditeur

---

## 5. Création des Utilisateurs

### Créer un utilisateur admin

1. Aller dans **Users**
2. Cliquer sur **"Add user"**
3. Configuration :
   - **Username** : `admin`
   - **Email** : `admin@clearing.sn`
   - **First name** : `Admin`
   - **Last name** : `System`
   - **Email verified** : `ON`
   - **Enabled** : `ON`
4. Cliquer sur **Create**

### Définir le mot de passe

1. Aller dans l'onglet **Credentials**
2. Cliquer sur **"Set password"**
3. **Password** : `Admin@123`
4. **Password confirmation** : `Admin@123`
5. **Temporary** : `OFF`
6. Cliquer sur **Save**

### Assigner les rôles

1. Aller dans l'onglet **Role mapping**
2. Cliquer sur **"Assign role"**
3. Sélectionner **ADMIN** et **USER**
4. Cliquer sur **Assign**

### Créer un utilisateur standard

Répéter le processus pour créer un utilisateur :
- **Username** : `user`
- **Email** : `user@clearing.sn`
- **Password** : `User@123`
- **Rôle** : `USER` seulement

---

## 6. Test de l'authentification

### Obtenir un token via cURL

```bash
# Pour l'admin
curl -X POST 'http://localhost:8080/realms/clearing-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=clearing-api-client' \
  -d 'client_secret=VOTRE_CLIENT_SECRET' \
  -d 'username=admin' \
  -d 'password=Admin@123' \
  -d 'grant_type=password'
```

### Tester l'API avec le token

```bash
# Sauvegarder le token
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# Tester l'endpoint public
curl http://localhost:8081/api/public/hello

# Tester l'endpoint user (nécessite le token)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/user/profile

# Tester l'endpoint admin (nécessite le rôle ADMIN)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/admin/dashboard
```

### Tester avec Swagger

1. Aller sur http://localhost:8081/swagger-ui.html
2. Cliquer sur **"Authorize"**
3. Dans **bearer-jwt**, entrer : `Bearer VOTRE_TOKEN`
4. Cliquer sur **"Authorize"** puis **"Close"**
5. Tester les endpoints

---

## 7. Intégration avec Angular

### Installation des dépendances

```bash
npm install keycloak-angular keycloak-js
```

### Configuration Keycloak (keycloak.config.ts)

```typescript
import { KeycloakConfig } from 'keycloak-js';

export const keycloakConfig: KeycloakConfig = {
  url: 'http://localhost:8080',
  realm: 'clearing-realm',
  clientId: 'clearing-api-client'
};
```

### Initialisation dans app.config.ts

```typescript
import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { keycloakConfig } from './keycloak.config';

function initializeKeycloak(keycloak: KeycloakService) {
  return () =>
    keycloak.init({
      config: keycloakConfig,
      initOptions: {
        onLoad: 'login-required',
        checkLoginIframe: false
      }
    });
}

export const appConfig: ApplicationConfig = {
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService]
    }
  ]
};
```

### Service HTTP avec token (http.service.ts)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HttpService {
  private apiUrl = 'http://localhost:8081/api';

  constructor(
    private http: HttpClient,
    private keycloak: KeycloakService
  ) {}

  private async getHeaders(): Promise<HttpHeaders> {
    const token = await this.keycloak.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  async get(endpoint: string): Promise<Observable<any>> {
    const headers = await this.getHeaders();
    return this.http.get(`${this.apiUrl}/${endpoint}`, { headers });
  }

  async post(endpoint: string, data: any): Promise<Observable<any>> {
    const headers = await this.getHeaders();
    return this.http.post(`${this.apiUrl}/${endpoint}`, data, { headers });
  }
}
```

---

## 📝 Checklist de vérification

- [ ] Keycloak est installé et fonctionne sur le port 8080
- [ ] Le realm `clearing-realm` est créé
- [ ] Le client `clearing-api-client` est configuré avec le bon secret
- [ ] Les rôles USER et ADMIN sont créés
- [ ] Au moins un utilisateur admin et un utilisateur standard sont créés
- [ ] Les utilisateurs ont les bons rôles assignés
- [ ] Le `application.properties` contient le bon client secret
- [ ] L'API Spring Boot démarre sans erreur
- [ ] Les endpoints publics sont accessibles sans token
- [ ] Les endpoints protégés nécessitent un token valide
- [ ] Swagger est accessible et fonctionnel
- [ ] Angular peut s'authentifier et appeler l'API

---

## 🔧 Dépannage

### Erreur : "Invalid issuer"
- Vérifier que `keycloak.auth-server-url` dans `application.properties` est correct
- S'assurer que Keycloak est accessible sur cette URL

### Erreur : "Invalid token"
- Le token a peut-être expiré (durée par défaut : 5 minutes)
- Générer un nouveau token

### Erreur : "Access Denied"
- Vérifier que l'utilisateur a les bons rôles
- Vérifier que les rôles sont bien configurés dans Keycloak

### CORS Error depuis Angular
- Vérifier que les Web Origins sont configurés dans le client Keycloak
- Vérifier la configuration CORS dans `SecurityConfig.java`

---

## 📚 Ressources

- [Documentation Keycloak](https://www.keycloak.org/documentation)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Keycloak Angular](https://github.com/mauriciovigolo/keycloak-angular)