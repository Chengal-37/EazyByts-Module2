# Stock Trading Simulation System

A full-stack Java application that provides a realistic stock trading simulation environment for users to practice trading strategies without financial risk.

## Features

- User authentication and authorization
- Real-time stock price updates
- Portfolio management
- Buy and sell stock transactions
- Performance tracking and analytics
- Responsive web interface
- Educational resources

## Technology Stack

- Backend:
  - Java 21
  - Spring Boot 3.2
  - Spring Security
  - Spring Data JPA
  - MySQL Database
  - JWT Authentication

- Frontend:
  - HTML5
  - CSS3
  - JavaScript
  - Bootstrap
  - Chart.js for analytics

## Prerequisites

- Java 21 or higher
- Maven
- MySQL 8.0 or higher
- Node.js and npm (for frontend development)

## Setup Instructions

1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```

2. Configure MySQL:
   - Create a database named `stock_trading_simulator`
   - Update `application.properties` with your database credentials

3. Build and run the backend:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. The application will be available at `http://localhost:8080`

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── stocktrading/
│   │           ├── config/
│   │           ├── controller/
│   │           ├── model/
│   │           ├── repository/
│   │           ├── service/
│   │           └── StockTradingApplication.java
│   └── resources/
│       ├── static/
│       ├── templates/
│       └── application.properties
└── test/
```

## API Documentation

The API documentation will be available at `http://localhost:8080/swagger-ui.html` when running the application.

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 