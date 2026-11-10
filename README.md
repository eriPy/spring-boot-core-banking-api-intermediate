# Banking System

This project was built primarily using Java, along with the following technologies and tools:

- Docker
- Apache Kafka
- Redis
- Mailpit
- Java libraries and dependencies

## Repository Structure

During the development of this project, several architectural, scalability, and design issues were identified.

To address these limitations, a second version of the application (V2) is planned. This version will focus on improving the database structure, implementing better data consistency checks related to IP addresses, and improving API integration with a frontend application.

V2 will be designed with a more realistic banking environment in mind, incorporating additional technologies and a more robust architecture.

## V1

Version 1 of the application was developed to simulate several banking operations, including:

- Transaction fees
- Deposits
- Interest payments
- Interest charges
- Withdrawals
- Transfers

The application stores user information, which can be updated in the future.

All these operations require a six-digit token as an additional verification mechanism. This is intended to prevent repeated requests and reduce the risk of backend abuse or database overload.

## Architecture

The application uses **Flyway** to manage database schema migrations, ensuring a consistent database structure throughout development.

It also implements scheduled tasks to process recurring banking operations, such as interest charges and interest payments.

**Redis** is used to cache frequently accessed user data, reducing the need for constant database queries. If Redis becomes unavailable, the application can fall back to the database to ensure that users can continue processing requests.

**Apache Kafka** is implemented to process and analyze user activity, helping detect suspicious behavior such as flagged transactions, smurfing, and automated bot activity.

Additionally, the application implements **rate limiting** to prevent incoming requests from overwhelming the backend or outpacing Kafka's event-processing capabilities. This also helps mitigate automated abuse, such as bot activity.

Finally, **JWT (JSON Web Tokens)** are used for authentication, allowing the application to identify and validate authenticated users.