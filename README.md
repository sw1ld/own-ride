# OwnRide

`OwnRide` is a lightweight Quarkus application for analyzing and managing Garmin FIT files.
I believe that your fitness data should belong to you, and only you.

## 🚀 Key Features

- **FIT File Upload:** Easily upload single or multiple FIT files via a web frontend
- **Statistics Dashboard:** Visualization of activity data per year
- **Details View:** Provide activity details in a list format as well as the route using open street maps
- **Data Persistence:** Metadata storage in a PostgreSQL database
- **HTTP API:** Access detailed activity data and statistics in JSON format

![Upload Screenshot](src/main/resources/screenshots/upload.png)

![Dashboard Screenshot](src/main/resources/screenshots/dashboard.png)

![Details Screenshot](src/main/resources/screenshots/details.png)


## 🚲 Why .FIT?

The **Flexible and Interoperable Data Transfer (FIT)** protocol is the industry standard for fitness devices (Garmin, Wahoo, etc.).

- **Efficiency:** Unlike XML-based formats, FIT is a **binary format**, making files significantly smaller and faster to process.
- **Rich Data:** It supports a vast array of sensor data (Heart Rate, Power, Cadence, Temperature) within a single message-based structure.
- **Extensibility:** Custom developer fields allow for device-specific metrics.

## 🛠️ Local Setup

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL** database

### Configuration

Create an `.env` file in the project directory and define the data source.
Here is an example for a local PostgreSQL instance:

```properties
_DEV_QUARKUS_DATASOURCE_DB_KIND=postgresql
_DEV_QUARKUS_DATASOURCE_USERNAME=someuser
_DEV_QUARKUS_DATASOURCE_PASSWORD=somepassword
_DEV_QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/fitdb
```

### Build

```shell script
./mvn clean install 
```

### Start

```shell script
./mvn quarkus:dev
```

The application is then accessible at [http://localhost:8080/fit/stats](http://localhost:8080/fit/stats).

## 📖 API Usage

The application provides both an HTML interface and a JSON API:

- **Web Frontend (Stats):** `GET /fit/stats` (with Header `Accept: text/html`)
- **JSON Statistics:** `GET /fit/stats?year=2025` (with Header `Accept: application/json`)
- **Activity Details:** `GET /fit/details?year=2025`
- **File Upload:** `POST /fit/upload` (Multipart Form Data)

## 🤝 Contribution

Contributions are welcome!
If you want to improve the project:

1. **Fork** the repository.
2. Create a **Feature Branch** (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push the branch (`git push origin feature/AmazingFeature`).
5. Open a **Pull Request**.

### Development & Testing

- **Code Style:** The project uses `spotless` with Google Java Format.
Run `./mvn spotless:apply` to format the code.
- **Run Tests:** `./mvn test`
- **Dev UI:** Accessible in development mode at [http://localhost:8080/q/dev/](http://localhost:8080/q/dev/).

## 🗺️ Roadmap / Open TODOs

- [ ] Add swagger docs
- [ ] Add pipeline for github
- [ ] Disable /q/dev endpoints for "prod usage"!
- [x] add junie file for "how to write code using AI" (see [.junie/CONTRIBUTING_AI.md](.junie/CONTRIBUTING_AI.md))
- [ ] Duplicate check during upload (ID field in FIT file?)
- [ ] Additional tags for filtering (Bike, Route, etc.)
- [ ] Expand database for metadata persistence
- [ ] Altitude profile calculation via GPS positions
- [ ] Speed graphs & slope display
- [ ] Performance optimization (caching, persistent pre-calculation)

---
*Developed with ❤️*
