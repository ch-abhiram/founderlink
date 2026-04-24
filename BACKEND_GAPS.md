# FounderLink Backend Gaps Documentation

This document explicitly lists the APIs and capabilities requested for the `Frontend` integration that are **missing, incomplete, or incorrectly implemented** on the microservices backend. The Angular frontend has been implemented with stub components (e.g., dummy views or optimistic UI) to handle these gracefully.

---

## 1. WebSocket Support for Real-Time Comms
- **Expectation**: The `messaging-service` and `notification-service` should expose WebSocket endpoints (e.g., via STOMP) for real-time pushing of new notifications and chat messages.
- **Reality**: The backend only exposes RESTful endpoints.
- **Frontend Mitigation**: 
  - `NotificationBellComponent` uses `setInterval` (30s) polling against `GET /notifications`.
  - `InboxComponent` uses `setInterval` (15s) polling against `GET /messages/conversation/{id}` when a chat is open.

## 2. API: Get All Users (Admin)
- **Expectation**: The Admin needs an endpoint to fetch all registered users to manage their roles (`GET /api/auth/users`).
- **Reality**: The `auth-service` and `user-service` do not expose an endpoint to retrieve a paginated or full list of users for administration.
- **Frontend Mitigation**: 
  - `src/app/features/admin/users/users.component.ts` implements a disabled dummy view explaining that the API is not yet available.

## 3. API: Get All Platform Investments (Admin)
- **Expectation**: The Admin needs a global ledger view of all investments across the platform (`GET /investments/all`).
- **Reality**: The `investment-service` only supports `GET /investments/me` and `GET /investments/startup/{id}`.
- **Frontend Mitigation**: 
  - `src/app/features/admin/investments/investments.component.ts` implements a disabled dummy view exposing the backend gap.

## 4. Razorpay Backend Order Workflow
- **Expectation**: Secure Razorpay integration requires generating an order on the server and verifying the signature upon completion.
- **Reality**: The `investment-service` `POST /investments` simply records raw investment intents directly, without engaging a Payment Gateway provider.
- **Frontend Mitigation**: 
  - `DetailComponent.proceedWithInvestment()` directly utilizes the Razorpay frontend SDK with a test key (`options.key`). It blindly fires `PUT /investments/{id}/status` passing `COMPLETED` when Razorpay succeeds on the client side, bypassing secure server verification. This is insecure and strictly for test/demo mode.

## 5. Follow / Unfollow System
- **Expectation**: Unfollowing a startup requires `DELETE /startups/{id}/follow` or similar.
- **Reality**: Backend exposes `POST /startups/{id}/follow`. Unfollowing is not natively supported or documented.
- **Frontend Mitigation**: The frontend `StartupService.unfollow()` hits a pseudo endpoint. It will likely throw a 404 until implemented.

---

### Action Items for Backend Team
1. Implement `messaging-service` WebSocket configuration (Spring WebSocket).
2. Wire up `razorpay-java` SDK to generate orders, and expose a webhook endpoint or verification endpoint.
3. Expose Admin Controller routes in `auth-service` and `investment-service` with `hasRole('ROLE_ADMIN')`.
