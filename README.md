# Teleshop - Mini E-commerce en Microservices (Spring Boot + RabbitMQ + Docker)

Ce projet est un TP d'introduction aux microservices avec Spring Boot 3+, Java 17, RabbitMQ, Postgres et Spring Cloud Gateway.

## Services
- `gateway` (Spring Cloud Gateway)
- `user-service` (utilisateurs)
- `order-service` (commandes, publie `OrderCreated`)
- `inventory-service` (stock, consomme `OrderCreated`, publie `StockUpdated`)
- `notification-service` (notifications, consomme `OrderCreated` et `StockUpdated`)

## Architecture
- API Gateway expose les routes:
  - `/users/**` → `user-service`
  - `/orders/**` → `order-service`
  - `/inventory/**` → `inventory-service`
  - `/notifications/**` → `notification-service`
- Communication asynchrone via RabbitMQ (topic exchange `teleshop.exchange`)
  - `order.created` → consommé par `inventory-service` et `notification-service`
  - `stock.updated` → consommé par `notification-service`
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

## Notes techniques
- Java 17, Spring Boot 3.2.x, Spring Cloud 2023.0.x (compatibilité stable)
- Packaging via Maven, image Docker basée sur Temurin JRE 17
- JPA `ddl-auto=update` (pour le TP; en prod, utiliser des migrations)

## Extensions (pour aller plus loin)
- Ajouter `payment-service` (évènement `OrderPaid`)
- Ajouter `shipping-service` (évènement `OrderShipped`)
- Tracer les flux d’évènements (diagrammes, logs corrélés)
- Tests d’intégration avec Testcontainers
