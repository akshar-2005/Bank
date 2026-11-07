# Banking System (Spring Boot)

A REST backend for a banking application built with Java and Spring Boot. It supports customer registration and login, bank accounts, deposits, withdrawals, fund transfers, paginated transaction history and loans with admin approval.

## Features

- Register and log in with JWT authentication; passwords are stored as BCrypt hashes
- Role-based access control (CUSTOMER and ADMIN)
- Customers can only access their own accounts
- Open accounts, deposit, withdraw and transfer money
- Transfers are atomic: debit, credit and the transaction record commit or roll back together (`@Transactional`)
- Optimistic locking (`@Version`) prevents lost updates on concurrent balance changes
- Paginated transaction history (page and size parameters, newest first)
- Loans: apply, EMI calculation, admin approval or rejection, disbursement to the account
- Global exception handling with clear JSON error responses (400, 404, 409)
- Request validation with Bean Validation
- Unit tests for the transfer logic with JUnit 5 and Mockito

## Tech Stack

Java 21, Spring Boot, Spring Web, Spring Data JPA (Hibernate), Spring Security, JWT (JJWT), MySQL, Maven, JUnit 5, Mockito, Lombok

## Architecture

```
Controller  ->  Service  ->  Repository  ->  MySQL
   (HTTP)     (business rules)  (JPA)
```

- Controllers receive requests and return DTOs
- Services hold the business rules and transactions
- Repositories talk to the database through Spring Data JPA
- DTOs keep entities and password hashes out of API responses
- A JWT filter reads the token on every request and sets the logged-in user

Money uses `BigDecimal` (never `double`). Enums are stored as text.

## Getting Started

Requirements: JDK 21 and MySQL 8.

1. Create the database:

```sql
CREATE DATABASE bankdb;
```

2. copy src/main/resources/application.properties.example to src/main/resources/application.properties and set your MySQL password.
3. Run the app:

```
./mvnw spring-boot:run
```

On Windows use `.\mvnw spring-boot:run`. The server starts on http://localhost:8080 and Hibernate creates the tables automatically.

4. Run the tests:

```
./mvnw test
```

### Creating an admin

Registration always creates a CUSTOMER. To create an admin, register a user and promote it in MySQL:

```sql
UPDATE bankdb.users SET role = 'ADMIN' WHERE email = 'admin@mail.com';
```

## API

Send the token from login in the header `Authorization: Bearer <token>` on every request except register and login.

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | /api/auth/register | Register a new customer | Public |
| POST | /api/auth/login | Log in and receive a JWT | Public |
| POST | /api/accounts | Open an account | Customer |
| GET | /api/accounts/{accountNumber} | View one of your accounts | Customer |
| POST | /api/accounts/{accountNumber}/deposit | Deposit money | Customer |
| POST | /api/accounts/{accountNumber}/withdraw | Withdraw money | Customer |
| GET | /api/accounts/{accountNumber}/transactions?page=0&size=10 | Paginated transaction history | Customer |
| POST | /api/transfers | Transfer money from your account | Customer |
| POST | /api/loans | Apply for a loan | Customer |
| GET | /api/loans/mine | List your loans | Customer |
| GET | /api/admin/loans/pending | List pending loans | Admin |
| POST | /api/admin/loans/{id}/approve | Approve a loan and credit the account | Admin |
| POST | /api/admin/loans/{id}/reject | Reject a loan | Admin |

### Example: transfer

```
POST /api/transfers
{
  "fromAccountNumber": "4554903848",
  "toAccountNumber": "8257071516",
  "amount": 200.00
}
```

### Example error response

```
{
  "status": 404,
  "error": "Not Found",
  "message": "Account not found",
  "timestamp": "2026-11-07T11:31:04"
}
```

## EMI formula

`EMI = P x r x (1 + r)^n / ((1 + r)^n - 1)` where P is the loan amount, r is the monthly interest rate and n is the number of months.

## Frontend

A SvelteKit and Tailwind CSS web app in the `frontend` folder that uses the REST API above.

Screens: register, login, dashboard with account cards, account page (deposit, withdraw, transfer, paginated transaction history), customer loans page, and an admin page to approve or reject loans. The JWT is kept in the browser and sent with every request.

Run it (the backend must be running on port 8080):

```
cd frontend
npm install
npm run dev
```

Then open http://localhost:5173. The backend allows this origin through a CORS setting in `SecurityConfig`.