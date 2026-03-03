# CodeBank Challenge

Build a **Bank System API** using the project architecture (`http -> application -> infra`) and clean code principles.

## Goal

Implement a backend service where users can:

- Create bank accounts
- Deposit money
- Withdraw money
- Transfer money between accounts (wire transfer)
- View account balance and transaction history

---

## Business Rules

1. Account number must be unique.
2. Balance can never go below 0.
3. Deposit amount must be greater than 0.
4. Withdraw amount must be greater than 0 and less than or equal to balance.
5. Transfer amount must be greater than 0.
6. Transfer must be atomic (debit + credit in one transaction).
7. It must not be possible to transfer to the same account.

---

## Required Features (MVP)

### 1) Accounts

- `POST /api/accounts`
	- Create account
	- Request: `ownerName`, `document`, optional `initialBalance`
	- Response: `id`, `accountNumber`, `ownerName`, `balance`, `createdAt`

- `GET /api/accounts/{id}`
	- Get account details and balance

### 2) Money Operations

- `POST /api/accounts/{id}/deposit`
	- Request: `amount`, optional `description`

- `POST /api/accounts/{id}/withdraw`
	- Request: `amount`, optional `description`

- `POST /api/wires`
	- Request: `fromAccountId`, `toAccountId`, `amount`, optional `description`

### 3) Ledger / Statement

- `GET /api/accounts/{id}/transactions`
	- Return list of transactions ordered by date desc

Each transaction should include at least:

- `id`
- `type` (`DEPOSIT`, `WITHDRAW`, `WIRE_OUT`, `WIRE_IN`)
- `amount`
- `description`
- `createdAt`
- `balanceAfter`

---

## Technical Requirements

1. Use layered architecture:
	 - `http`: controllers, DTOs, advice
	 - `application`: domain, services, ports
	 - `infra`: entities, repositories, adapters, mappers
2. Use Flyway migrations for schema changes.
3. Use validation annotations on request DTOs.
4. Use global exception handling (`ApiExceptionHandler`).
5. Use MapStruct for persistence mapping (no manual mapper methods inside adapters).
6. Write at least:
	 - Unit tests for core business rules
	 - Integration/e2e tests for critical flows (create + deposit + withdraw + transfer)
7. Implement Swagger/OpenAPI documentation:
	- Expose OpenAPI spec
	- Expose Swagger UI
	- Document request/response models for all endpoints
	- Document error responses (`400`, `404`, `409`)
	- If JWT bonus is implemented, document Bearer authentication scheme in Swagger

---

## Suggested Domain Model

### Account

- `id: UUID`
- `accountNumber: String`
- `ownerName: String`
- `document: String`
- `balance: BigDecimal`
- `createdAt: Instant`

### Transaction

- `id: UUID`
- `accountId: UUID`
- `type: ENUM`
- `amount: BigDecimal`
- `description: String`
- `balanceAfter: BigDecimal`
- `createdAt: Instant`

### WireTransfer

- `id: UUID`
- `fromAccountId: UUID`
- `toAccountId: UUID`
- `amount: BigDecimal`
- `description: String`
- `createdAt: Instant`

---

## Required Error Scenarios

The API must return proper HTTP codes and error payloads:

- `400 Bad Request` for validation/business errors (invalid amount, same source/target, etc.)
- `404 Not Found` when account/transfer does not exist
- `409 Conflict` when uniqueness rules fail (e.g., duplicated account number)

Error response example:

```json
{
	"code": "INSUFFICIENT_FUNDS",
	"message": "Account does not have enough balance",
	"timestamp": "2026-03-02T23:30:00Z"
}
```

---

## Minimum Flyway Migrations

1. `V1__create_accounts_table.sql`
2. `V2__create_transactions_table.sql`
3. `V3__create_wire_transfers_table.sql`

Use DB constraints for data integrity where possible:

- Unique account number
- Foreign keys for account/transaction/transfer relations
- Check amount > 0 (if your DB supports it)

---

## Acceptance Criteria

Your implementation is considered complete when:

- Accounts can be created and queried
- Deposit/withdraw update balances correctly
- Transfer debits one account and credits another atomically
- Transaction history is persisted and queryable
- Validation and domain errors return clear API responses
- Swagger UI is available and describes all implemented endpoints
- All tests pass

---

## Bonus ⭐ — JWT + Secure Wires

Implement authentication and secure wire transfers.

### Bonus Requirements

1. Add auth endpoints:
	 - `POST /api/auth/register`
	 - `POST /api/auth/login` -> returns JWT
2. Protect money endpoints with JWT bearer auth.
3. Allow transfer only if token subject matches source account owner.
4. Add roles:
	 - `USER` can operate only own accounts
	 - `ADMIN` can view all accounts/transactions
5. Add token expiration and refresh strategy (optional extra bonus).

### “JWT encrypting wires” interpretation

At minimum for this bonus:

- **Sign** JWT tokens (integrity + auth)
- For extra security, support **encrypted transfer metadata** at rest (optional):
	- encrypt transfer description or external reference using app key

---

## Submission Notes (for developers)

- Keep commits small and meaningful.
- Document assumptions in README.
- Include sample cURL requests.
- Explain how to run with `dev` (H2) and `prod` (PostgreSQL + Docker).

Good luck 🚀

