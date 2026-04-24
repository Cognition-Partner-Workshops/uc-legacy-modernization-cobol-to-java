# CardDemo Modernization — Running the Demo

## Prerequisites

- **Node.js 18+** — for the React frontend
- **Java 17+** — for the Spring Boot backend
- **Maven 3.8+** — for building the backend (or use the included Maven wrapper)

## Quick Start

### 1. Start the Backend (Spring Boot + H2)

```bash
cd demo/backend
mvn spring-boot:run
```

The backend starts at **http://localhost:8080** with:
- REST API at `/api/cards`
- H2 Console at `/h2-console` (JDBC URL: `jdbc:h2:mem:carddemo`, user: `sa`, no password)
- 10 sample card records and 14 account records pre-loaded from the original mainframe data files

### 2. Start the Frontend (React)

```bash
cd demo/frontend
npm install
npm start
```

The React app opens at **http://localhost:3000** and connects to the Spring Boot backend.

## Demo Walk-Through

### Card List (replaces COCRDLI.bms / COCRDLIC.cbl)
- **URL**: http://localhost:3000/
- Shows all credit cards in a sortable table
- Use the search box to filter by Account Number
- Click a card number to view details, or use the Edit link

### Card Detail (replaces COCRDSL.bms / COCRDSLC.cbl)
- **URL**: http://localhost:3000/cards/{cardNumber}
- Displays read-only card information: Account Number, Card Number, Name on Card, Active Status, Expiry Date
- Click "Edit Card" to switch to update mode

### Card Update (replaces COCRDUP.bms / COCRDUPC.cbl)
- **URL**: http://localhost:3000/cards/{cardNumber}/edit
- Editable fields: Name on Card, Active Status (Y/N), Expiry Date
- Account Number and Card Number are read-only (same as the mainframe — account is PROT on the update screen)
- Validation rules match the original COBOL: active status must be Y or N, month 01-12, year 1950-2099

### Green Screen Preview
- **URL**: http://localhost:3000/green-screen
- Side-by-side comparison showing the original IBM 3270 terminal screens
- Three tabs: Card List, Card Detail, Card Update
- Field positions match the BMS POS coordinates from the original map definitions
- Colors match the BMS COLOR attributes: blue for system fields, turquoise for labels, yellow for titles

## API Endpoints

| Method | URL | Description | COBOL Equivalent |
|--------|-----|-------------|------------------|
| GET | `/api/cards` | List all cards | `COCRDLIC.cbl` → `9000-READ-FORWARD` |
| GET | `/api/cards?accountId=00000000050` | Filter by account | `COCRDLIC.cbl` → filtered browse |
| GET | `/api/cards/{cardNumber}` | Get card detail | `COCRDSLC.cbl` → `9100-GETCARD-BYACCTCARD` |
| PUT | `/api/cards/{cardNumber}` | Update card | `COCRDUPC.cbl` → `9100-UPDATE-CARD` |

## Project Structure

```
demo/
├── frontend/                  # React application
│   ├── src/
│   │   ├── App.jsx           # Router + navigation
│   │   ├── api/cardApi.js    # Axios HTTP client
│   │   └── components/
│   │       ├── CardListPage.jsx        # → COCRDLI.bms
│   │       ├── CardDetailPage.jsx      # → COCRDSL.bms
│   │       ├── CardUpdatePage.jsx      # → COCRDUP.bms
│   │       └── GreenScreenPreview.jsx  # 3270 terminal mockup
│   └── package.json
├── backend/                   # Spring Boot application
│   ├── src/main/java/com/carddemo/
│   │   ├── CardDemoApplication.java
│   │   ├── model/             # JPA entities from copybooks
│   │   ├── repository/        # Spring Data JPA repos
│   │   ├── service/           # Business logic from COBOL
│   │   └── controller/        # REST API endpoints
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── schema.sql         # DDL from copybook layouts
│   │   └── data.sql           # Seed data from VSAM files
│   └── pom.xml
├── business-rules/            # Extracted BRE documentation
│   ├── card-detail-view-bre.md
│   ├── card-update-bre.md
│   ├── data-model.md
│   └── screen-mapping.md
└── README.md                  # This file
```

## Troubleshooting

- **CORS errors**: The backend is configured to allow requests from `http://localhost:3000`. If you change the frontend port, update `CorsConfig.java`.
- **Port conflicts**: Backend defaults to 8080, frontend to 3000. Change in `application.properties` or `package.json` if needed.
- **H2 Console**: Navigate to `http://localhost:8080/h2-console` and use JDBC URL `jdbc:h2:mem:carddemo` to browse the data directly.
