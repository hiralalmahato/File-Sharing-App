import { useEffect } from "react";
import { useAuth } from "@clerk/clerk-react";
import axios from "axios";

const AxiosClerkInterceptor = ({ children }) => {
    const { getToken, isSignedIn } = useAuth();

    useEffect(() => {
        const interceptorId = axios.interceptors.request.use(async (config) => {
            if (!isSignedIn) {
                return config;
            }

            const headers = config.headers || {};
            if (headers.Authorization) {
                return config;
            }

            const token = await getToken();
            if (token) {
                config.headers = {
                    ...headers,
                    Authorization: `Bearer ${token}`,
                };
            }

            return config;
        });

        return () => {
            axios.interceptors.request.eject(interceptorId);
        };
    }, [getToken, isSignedIn]);

    return children;
};

export default AxiosClerkInterceptor;
