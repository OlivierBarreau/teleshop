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

- Créer une commande (publie OrderCreated)
```bash
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"productId":1,"quantity":2}'
```

- Lister les produits (stock)
```bash
curl http://localhost:8080/inventory/products
```

- Lister les notifications
```bash
curl http://localhost:8080/notifications
```

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
- `gateway`
  - Smoke: `GatewayApplicationTests` (contextLoads)

Exécution des tests (depuis la racine, un service à la fois):
```bash
pushd user-service && mvn -q test && popd
pushd order-service && mvn -q test && popd
pushd inventory-service && mvn -q test && popd
pushd notification-service && mvn -q test && popd
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
