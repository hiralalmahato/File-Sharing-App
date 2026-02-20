import {BrowserRouter, Route, Routes} from 'react-router-dom';
import Landing from "./Pages/Landing.jsx"
import Dashboard from "./Pages/Dashboard.jsx"
import Upload from "./Pages/Upload.jsx"
import MyFiles from "./Pages/MyFiles.jsx"
import Subscription from "./Pages/Subscription.jsx"
import Transactions from "./Pages/Transactions.jsx"
import {RedirectToSignIn, SignedIn, SignedOut} from "@clerk/clerk-react";
import {Toaster} from 'react-hot-toast'
import { UserCreditsProvider } from './Context/UserCreditsContext.jsx';

const App=()=>{
  return(
    <UserCreditsProvider>
    <BrowserRouter>
        <Toaster />
        <Routes>
            <Route path="/" element={<Landing />} />
                    <Route path="/dashboard" element={
                        <>
                            <SignedIn><Dashboard /></SignedIn>
                            <SignedOut><RedirectToSignIn /></SignedOut>
                        </>
                    } />
                    <Route path="/upload" element={
                        <>
                            <SignedIn><Upload /></SignedIn>
                            <SignedOut><RedirectToSignIn /></SignedOut>
                        </>
                    } />
                    <Route path="/my-files" element={
                        <>
                            <SignedIn><MyFiles /></SignedIn>
                            <SignedOut><RedirectToSignIn /></SignedOut>
                        </>
                    } />
                    <Route path="/subscriptions" element={
                        <>
                            <SignedIn><Subscription /></SignedIn>
                            <SignedOut><RedirectToSignIn /></SignedOut>
                        </>
                    } />
                    <Route path="/transactions" element={
                        <>
                            <SignedIn><Transactions /></SignedIn>
                            <SignedOut><RedirectToSignIn /></SignedOut>
                        </>
                    } />
                    
                    <Route path="/*" element={<RedirectToSignIn />} />
        </Routes>
    </BrowserRouter>
    </UserCreditsProvider>
  )
}

export default App;