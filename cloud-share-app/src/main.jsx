import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {ClerkProvider} from "@clerk/clerk-react";
import AxiosClerkInterceptor from "./lib/AxiosClerkInterceptor.jsx";

// Clerk may show development-mode warnings locally; this is expected.
// Production deployment should use production Clerk publishable/secret keys.
const clerkPubKey = import.meta.env.VITE_CLERK_PUBLISHABLE_KEY

createRoot(document.getElementById('root')).render(
  <ClerkProvider publishableKey={clerkPubKey}>
    <AxiosClerkInterceptor>
      <App />
    </AxiosClerkInterceptor>
  </ClerkProvider>
)
