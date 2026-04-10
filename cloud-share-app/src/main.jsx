import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {ClerkProvider} from "@clerk/clerk-react";
import AxiosClerkInterceptor from "./lib/AxiosClerkInterceptor.jsx";

const clerkPubKey = import.meta.env.VITE_CLERK_PUBLISHABLE_KEY

createRoot(document.getElementById('root')).render(
  <ClerkProvider publishableKey={clerkPubKey}>
    <AxiosClerkInterceptor>
      <App />
    </AxiosClerkInterceptor>
  </ClerkProvider>
)
