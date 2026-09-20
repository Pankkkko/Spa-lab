import HomeWelcome from "../components/HomeWelcome";
import HomeAction from "../components/HomeAction";
import "../styles/home.css";

interface HomePageProps {
    isAuthenticated: boolean;
    onLogin: () => void;
    onLogout: () => void;
}

function HomePage({
    isAuthenticated,
    onLogin,
    onLogout,
}: HomePageProps) {
    return (
        <main className="home-page">
            <section className="home-card">
                <HomeWelcome />

                <HomeAction
                    isAuthenticated={isAuthenticated}
                    onLogin={onLogin}
                    onLogout={onLogout}
                />
            </section>
        </main>
    );
}

export default HomePage;

