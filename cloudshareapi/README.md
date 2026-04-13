# File-Sharing-App-Backend

## Required Environment Variables

Set these in Render for production:

- `SPRING_DATA_MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>/<db>?retryWrites=true&w=majority`
- `CLERK_SECRET_KEY=sk_live_xxxxxxxx`
- `CLERK_PUBLISHABLE_KEY=pk_live_xxxxxxxx`
- `CLERK_ISSUER=https://<your-production-clerk-domain>`
- `CLERK_JWKS_URL=https://<your-production-clerk-domain>/.well-known/jwks.json`
- `CLERK_WEBHOOK_SECRET=whsec_xxxxxxxx`
- `APP_FRONTEND_URL=https://<your-vercel-app>.vercel.app`
- `CLOUDINARY_CLOUD_NAME=<your-cloudinary-cloud-name>`
- `CLOUDINARY_API_KEY=<your-cloudinary-api-key>`
- `CLOUDINARY_API_SECRET=<your-cloudinary-api-secret>`
- `RAZORPAY_KEY_ID=rzp_live_xxxxxxxx`
- `RAZORPAY_KEY_SECRET=<your-razorpay-secret>`

## Important

Do not use Clerk development keys in production. In Clerk Dashboard, switch to the Production instance and copy live keys (`pk_live` and `sk_live`) into your Render and Vercel environments.
