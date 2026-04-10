# Cloud Share App

A full-stack File Sharing Application built with:

## 🚀 Tech Stack

- Frontend: React + Vite
- Backend: Spring Boot
- Database: MongoDB
- Payments: Razorpay

## ✨ Features

- File Upload
- Public File Sharing
- Dashboard
- Subscription Plans
- Payment Integration
- Transaction History

## 🔧 Setup Instructions

### Frontend
npm install
npm run dev

## Production Environment Variables (Vercel)

- `VITE_CLERK_PUBLISHABLE_KEY=pk_live_xxxxxxxx`
- `VITE_API_URL=https://file-sharing-app-backend-1bb7.onrender.com/api/v1.0`

Use Clerk Production instance keys in production (`pk_live...`), not development keys (`pk_test...`).

### Backend
mvn clean install
mvn spring-boot:run