# Car Parking Management System

This project includes:

- **Java backend** using JDK built-in HTTP server (`backend/`)
- **JavaScript frontend** using vanilla HTML/CSS/JS (`frontend/`)

## Backend (Java)

No external dependencies are required.

```bash
cd backend
mkdir -p out
javac -d out $(find src/main/java -name "*.java")
java -cp out com.parking.ParkingManagementApplication
```

Runs on `http://localhost:8080` with APIs:

- `GET /api/parking/health`
- `GET /api/parking/dashboard`
- `POST /api/parking/check-in` with `{ "vehicleNumber": "KA01AB1234" }`
- `POST /api/parking/check-out` with `{ "ticketId": "TKT-XXXX" }`

## Backend tests

```bash
cd backend
mkdir -p out
javac -d out $(find src/main/java src/test/java -name "*.java")
java -cp out com.parking.service.ParkingServiceTest
```

## Frontend (JavaScript)

```bash
cd frontend
python3 -m http.server 5500
```

Open: `http://localhost:5500`.

> Ensure backend is running first.

### Optional API base override

If your backend runs on a different host/port, open browser devtools and run:

```js
localStorage.setItem('parkingApiBase', 'http://localhost:8080/api/parking')
```

Then refresh the page.
