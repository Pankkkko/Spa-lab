interface HomeActionProps {
    isAuthenticated: boolean;
    onLogin: () => void;
    onLogout: () => void;
}

function HomeAction({
    isAuthenticated,
    onLogin,
    onLogout,
}: HomeActionProps) {
    if (isAuthenticated) {
        return (
            <button
                className="home-button logout-button"
                onClick={onLogout}
            >
                Cerrar sesión
            </button>
        );
    }

    return (
        <button
            className="home-button"
            onClick={onLogin}
        >
            Iniciar sesión
        </button>
    );
}

export default HomeAction;

