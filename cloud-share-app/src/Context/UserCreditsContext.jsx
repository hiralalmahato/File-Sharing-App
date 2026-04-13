import {createContext, useCallback, useEffect, useRef, useState} from "react";
import {useAuth} from "@clerk/clerk-react";
import axios from "axios";
import {apiEndpoints} from "../util/apiEndpoints.js";
import toast from "react-hot-toast";

export const UserCreditsContext = createContext();

export const UserCreditsProvider = ({children}) => {
    const [credits, setCredits] = useState(5);
    const [loading, setLoading] = useState(false);
    const [creditsError, setCreditsError] = useState("");
    const {getToken, isSignedIn} = useAuth();
    const isFetchingRef = useRef(false);
    const lastAttemptRef = useRef(0);


    //Function to fetch the user credits that can be called from anywhere
    const fetchUserCredits = useCallback(async (force = false) => {
        if (!isSignedIn) {
            setCredits(5);
            setCreditsError("");
            return;
        }

        const now = Date.now();
        if (!force && isFetchingRef.current) {
            return;
        }

        // Back off briefly after a failed request to avoid call loops and spam.
        if (!force && now - lastAttemptRef.current < 5000) {
            return;
        }

        isFetchingRef.current = true;
        lastAttemptRef.current = now;

        setLoading(true);

        try {
            const token = await getToken();
            const response = await axios.get(apiEndpoints.GET_CREDITS, {headers: {Authorization: `Bearer ${token}`}});
            if (response.status === 200) {
                setCredits(response.data.credits);
                setCreditsError("");
            } else {
                setCreditsError("Unable to load data");
            }
        }catch (error) {
            setCreditsError("Unable to load data");
            toast.error("Unable to load credits right now.");
        }finally {
            isFetchingRef.current = false;
            setLoading(false);
        }
    },[getToken, isSignedIn]);

    useEffect(() => {
        if (isSignedIn) {
            fetchUserCredits(true);
        }
    }, [fetchUserCredits, isSignedIn]);


    const updateCredits = useCallback(newCredits => {
        setCredits(newCredits);
    }, []);


    const contextValue = {
        credits,
        setCredits,
        loading,
        creditsError,
        fetchUserCredits,
        updateCredits
    }

    return (
        <UserCreditsContext.Provider value={contextValue}>
            {children}
        </UserCreditsContext.Provider>
    )
}