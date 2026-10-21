# Tabkeeper - Architecture

The general layout for Tabkeeper is as follows:
* API Gateway
* 4 microservices:
  * **User Service** - handles user creation/details, as well as auth.
  * **Group Service** - handles group creation, memberships, invitations.
  * **Expense Service** - handles expense adding, returns registry, balance calculations.
  * **Notification Service** - handles notifications for various events throughout the application.

Furthermore, the following components will support the system:
* **User Service Database** - PostgreSQL
* **Group Service Database** - PostgreSQL
* **Expense Service Database** - PostgreSQL
* **Message Queue** - RabbitMQ; serves to propagate events across the system.
* **Discovery Server** - Eureka; enables service discovery. 