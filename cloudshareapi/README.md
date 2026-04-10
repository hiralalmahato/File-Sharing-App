# File-Sharing-App-Backend

## Required Environment Variables

Set these in Render for production:

- `CLERK_SECRET_KEY=sk_live_xxxxxxxx`
- `CLERK_PUBLISHABLE_KEY=pk_live_xxxxxxxx`
- `CLERK_ISSUER=https://<your-production-clerk-domain>`
- `CLERK_JWKS_URL=https://<your-production-clerk-domain>/.well-known/jwks.json`
- `CLERK_WEBHOOK_SECRET=whsec_xxxxxxxx`
- `APP_FRONTEND_URL=https://YOUR_VERCEL_APP_URL.vercel.app`

## Important

Do not use Clerk development keys in production. In Clerk Dashboard, switch to the Production instance and copy live keys (`pk_live` and `sk_live`) into your Render and Vercel environments.
