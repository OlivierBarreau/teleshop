# Teleshop - Mini E-commerce en Microservices (Spring Boot + RabbitMQ + Docker)

Ce projet est un TP d'introduction aux microservices avec Spring Boot 3+, Java 17, RabbitMQ, Postgres et Spring Cloud Gateway.

## Services
- `gateway` (Spring Cloud Gateway)
- `user-service` (utilisateurs)
- `order-service` (commandes, publie `OrderCreated`)
- `inventory-service` (stock, consomme `OrderCreated`, publie `StockUpdated`)
- `notification-service` (notifications, consomme `OrderCreated` et `StockUpdated`)
- `payment-service` (paiements, consomme `OrderCreated`, publie `OrderPaid`)
- `shipping-service` (expédition, consomme `OrderPaid`, publie `OrderShipped`)

## Architecture
- API Gateway expose les routes:
  - `/users/**` → `user-service`
  - `/orders/**` → `order-service`
  - `/inventory/**` → `inventory-service`
  - `/notifications/**` → `notification-service`
  - `/payments/**` → `payment-service`
  - `/shipments/**` → `shipping-service`
- Communication asynchrone via RabbitMQ (topic exchange `teleshop.exchange`)
  - `order.created` → consommé par `inventory-service`, `notification-service`, et `payment-service`
  - `stock.updated` → consommé par `notification-service`
  - `order.paid` → consommé par `shipping-service`
  - `order.shipped` → disponible pour d'autres services
- 1 base Postgres par service (via Docker Compose)

## Lancer le projet
Pré-requis: Docker et Docker Compose installés.

```bash
# À la racine du repo
docker compose up --build
```

- Gateway: http://localhost:8080
- RabbitMQ UI: http://localhost:15672 (user/pass: guest/guest)

## Endpoints rapides (exemples)

### Users

- Créer un utilisateur
```bash
curl -X POST http://localhost:8080/users/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Alice","email":"alice@example.com","password":"secret"}'
```

- Récupérer un utilisateur
```bash
curl http://localhost:8080/users/1
```

- Supprimer un utilisateur
```bash
curl -i -X DELETE http://localhost:8080/users/1
```

**Comportement:** `DELETE /users/{id}` retourne **409 Conflict** si l'utilisateur a des commandes ouvertes (status `CREATED`). Retourne **204 No Content** si aucune commande ouverte n'existe et la suppression réussit.

**Exemples:**

- Créer un utilisateur avec une commande ouverte (expect 409)
```bash
# 1. Créer l'utilisateur
curl -X POST http://localhost:8080/users/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Eve","email":"eve@example.com","password":"secret"}'
# Réponse: {"id":2,"name":"Eve","email":"eve@example.com","password":"secret"}

# 2. Créer une commande pour cet utilisateur
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"userId":2,"productId":1,"quantity":1}'

# 3. Tenter de supprimer l'utilisateur (FAIL - 409)
curl -i -X DELETE http://localhost:8080/users/2
# Réponse: HTTP/1.1 409 Conflict
# {"message":"User has open orders"}
```

- Créer un utilisateur sans commande (expect 204)
```bash
# 1. Créer l'utilisateur
curl -X POST http://localhost:8080/users/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"NoOrders","email":"no@example.com","password":"x"}'
# Réponse: {"id":3,"name":"NoOrders","email":"no@example.com","password":"x"}

# 2. Supprimer l'utilisateur (SUCCESS - 204)
curl -i -X DELETE http://localhost:8080/users/3
# Réponse: HTTP/1.1 204 No Content
```

**Note:** "Commandes ouvertes" = commandes avec status `CREATED`. Configurable pour inclure d'autres statuts (`PAID`, `PROCESSING`) dans le futur.

### Orders

- Créer une commande (publie OrderCreated)
```bash
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"productId":1,"quantity":2}'
```

### Inventory

- Lister les produits (stock)
```bash
curl http://localhost:8080/inventory/products
```

### Notifications

- Lister les notifications
```bash
curl http://localhost:8080/notifications
```

### Payments

- Lister les paiements
```bash
curl http://localhost:8080/payments
```

- Traiter un paiement (utiliser `pm_card_test` comme méthode pour simuler le succès)
```bash
curl -X POST http://localhost:8080/payments/1/process \
  -H 'Content-Type: application/json' \
  -d '{"paymentMethodId":"pm_card_test"}'
```

### Shipping

- Lister les expéditions
```bash
curl http://localhost:8080/shipments
```

- Expédier une commande
```bash
curl -X POST http://localhost:8080/shipments/1/ship
```

## Notes techniques
- Java 17, Spring Boot 3.2.x, Spring Cloud 2023.0.x (compatibilité stable)
- Packaging via Maven, image Docker basée sur Temurin JRE 17
- JPA `ddl-auto=update` (pour le TP; en prod, utiliser des migrations)

## Tests

Couverture minimale par service:
- `user-service`
  - Data JPA: `UserRepositoryTest` (H2)
  - Web MVC: `UserControllerTest` (MockMvc + @MockBean)
- `order-service`
  - Data JPA: `OrderRepositoryTest`
  - Web MVC: `OrderControllerTest` (vérifie la publication d’évènement)
- `inventory-service`
  - Data JPA: `ProductRepositoryTest`
  - Web MVC: `InventoryControllerTest`
  - Unit (Mockito): `InventoryListenerTest` (réduction du stock + publish `stock.updated`)
- `notification-service`
  - Data JPA: `NotificationRepositoryTest` (inclut `deleteByUserId`)
  - Web MVC: `NotificationControllerTest`
  - Unit (Mockito): `NotificationListenerTest` (réagit à `order.created`, `stock.updated`, `user.deleted`)
- `payment-service`
  - Unit (Mockito): `PaymentServiceTest` (création, traitement, gestion d'erreurs)
  - Web MVC: `PaymentControllerTest` (endpoints REST, validation)
  - Unit (Mockito): `OrderListenerTest` (consomme `order.created`, calcul montant)
- `gateway`
  - Smoke: `GatewayApplicationTests` (contextLoads)

Exécution des tests (depuis la racine, un service à la fois):
```bash
pushd user-service && mvn -q test && popd
pushd order-service && mvn -q test && popd
pushd inventory-service && mvn -q test && popd
pushd notification-service && mvn -q test && popd
pushd payment-service && mvn -q test && popd
pushd gateway && mvn -q test && popd
```

Notes:
- Chaque service utilise H2 en test; pas besoin de Postgres/RabbitMQ.
- Les tests `@WebMvcTest` simulent (mock) les dépendances; les listeners sont testés en pur Mockito.
- Maven au niveau racine n’a pas de POM agrégateur; lancer `mvn test` dans chaque module (voir ci-dessus). Un POM parent peut être ajouté ultérieurement si besoin.

## Extensions (pour aller plus loin)
- ✅ Ajouté `payment-service` (évènement `OrderPaid`)
- ✅ Ajouté `shipping-service` (évènement `OrderShipped`)
- Tracer les flux d'évènements (diagrammes, logs corrélés)
- Tests d'intégration avec Testcontainers
