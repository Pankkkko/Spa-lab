interface CatalogoNavbarProps {
    isAuthenticated: boolean;
    onBack: () => void;
    onLogin: () => void;
    onLogout: () => void;
}

export default function CatalogoNavbar({
    isAuthenticated,
    onBack,
    onLogin,
    onLogout,
}: CatalogoNavbarProps) {
    return (
        <nav className="catalogo-navbar">
            <div className="catalogo-navbar-brand">
                <div className="catalogo-navbar-logo">P360</div>
                <span>Pedidos360</span>
            </div>

            <div className="catalogo-navbar-actions">
                <button type="button" onClick={onBack}>
                    Inicio
                </button>

                {isAuthenticated ? (
                    <button type="button" onClick={onLogout}>
                        Cerrar sesión
                    </button>
                ) : (
                    <button type="button" onClick={onLogin}>
                        Iniciar sesión
                    </button>
                )}
            </div>
        </nav>
    );
}