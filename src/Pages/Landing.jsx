import HeroSection from '../Landing/HeroSection'
import FeaturesSection from '../Landing/FeaturesSection'
import PricingSection from '../Landing/PricingSection'
import TestimonialsSection from '../Landing/TestimonialsSection'
import CTASection from '../Landing/CTASection'
import Footer from '../Landing/Footer'
import { features,pricingPlans,testimonials } from '../assets/data'
import {useClerk, useUser} from "@clerk/clerk-react";
import {useNavigate} from "react-router-dom";
import {useEffect} from "react";


const Landing=()=>{
    const {openSignIn, openSignUp } = useClerk();
        const {isSignedIn } = useUser();
        const navigate = useNavigate();
    
        useEffect(() => {
            if (isSignedIn) {
                navigate("/dashboard");
            }
        }, [isSignedIn, navigate]);
    
    return(
        <div className="landing-page bg-linear-to-b  from-gray-50 to gray-100">
        <HeroSection openSignIn={openSignIn} openSignUp={openSignUp} />
        <FeaturesSection features={features}/>
        <PricingSection pricingPlans={pricingPlans} openSignUp={openSignUp} />
        <TestimonialsSection testimonials={testimonials} />
        <CTASection openSignUp={openSignUp} />
        <Footer/>

        </div>
    )
}
export default Landing;
