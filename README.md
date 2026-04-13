# Cloud Share - Full Stack File Sharing App

Cloud Share is a full-stack web application where users can upload files, manage them from a dashboard, share public links, and purchase credits/subscriptions for uploads.

## Live Demo

- Frontend: https://file-sharing-app-pl93.vercel.app/
- Backend: https://file-sharing-app-a6r7.onrender.com

## Features

- Secure authentication and user management with Clerk
- File upload and personal dashboard
- Public/private file sharing toggle
- Public file view by shareable link
- Download and delete file support
- Credit-based usage model
- Razorpay payment integration
- Transaction history page

## Tech Stack

### Frontend

- React 19 + Vite
- React Router
- Axios
- Tailwind CSS
- Clerk React SDK

### Backend

- Java 21
- Spring Boot 3
- Spring Security
- MongoDB (Spring Data MongoDB)
- Razorpay Java SDK

### Deployment

- Frontend: Vercel
- Backend: Render

## Project Structure

```text
File Sharing App/
|- cloud-share-app/    # React + Vite frontend
|- cloudshareapi/      # Spring Boot backend
```

## Getting Started

## 1) Clone the repository

```bash
git clone <your-repo-url>
cd "File Sharing App"
```

## 2) Run the frontend

```bash
cd cloud-share-app
npm install
npm run dev
```

Frontend runs by default on `http://localhost:5173`.

## 3) Run the backend

```bash
cd cloudshareapi
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend runs by default on `http://localhost:8080`.

## Environment Variables

Set these before running in production.

### Frontend (`cloud-share-app`)

- `VITE_CLERK_PUBLISHABLE_KEY` (use production key, starts with `pk_live_`)
- `VITE_API_URL` (example: `https://file-sharing-app-a6r7.onrender.com`)

### Backend (`cloudshareapi`)

- `SPRING_DATA_MONGODB_URI`
- `CLERK_SECRET_KEY` (production key, starts with `sk_live_`)
- `CLERK_PUBLISHABLE_KEY`
- `CLERK_ISSUER`
- `CLERK_JWKS_URL`
- `CLERK_WEBHOOK_SECRET`
- `APP_FRONTEND_URL` (set to `https://file-sharing-app-pl93.vercel.app/`)
- `RAZORPAY_KEY_ID`
- `RAZORPAY_KEY_SECRET`

## API Base URL Notes

Frontend API calls are built from `VITE_API_URL` in `cloud-share-app/src/util/apiEndpoints.js` and currently target routes like:

- `/files/my`
- `/files/upload`
- `/files/public/{fileId}`
- `/payments/create-order`
- `/payments/verify-payment`

Make sure `VITE_API_URL` points to your backend origin without trailing slashes.

## Security Notes

- Use Clerk production credentials in production.
- Do not commit secrets in Git.
- Keep backend CORS allowlist (`APP_FRONTEND_URL`) aligned with your deployed frontend URL.

## License

This project is for educational/personal use. Add a LICENSE file if you plan to distribute it publicly.
