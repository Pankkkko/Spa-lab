
interface CatalogoNavbarProps {
    isAuthenticated: boolean;
    nombreUsuario?: string;
    cantidadCarrito: number;
    paginaActual: 'inicio' | 'catalogo';
    onInicio: () => void;
    onCatalogo: () => void;
    onAbrirPedidos?: () => void;
    onLogin: () => void;
    onLogout: () => void;
    onAbrirCarrito: () => void;
}

export default function CatalogoNavbar({
    isAuthenticated,
    nombreUsuario,
    cantidadCarrito,
    paginaActual,
    onInicio,
    onCatalogo,
    onAbrirPedidos,
    onLogin,
    onLogout,
    onAbrirCarrito,
}: CatalogoNavbarProps) {
    return (
        <nav className="catalogo-navbar">
            <div className="catalogo-navbar-brand">
                <div className="catalogo-navbar-logo">P360</div>
                <span>Pedidos360</span>
            </div>

            <div className="catalogo-navbar-actions">
                <button
                    type="button"
                    className={
                        paginaActual === 'inicio'
                            ? 'navbar-link navbar-link-active'
                            : 'navbar-link'
                    }
                    onClick={onInicio}
                    aria-current={
                        paginaActual === 'inicio' ? 'page' : undefined
                    }
                >
                    Inicio
                </button>

                <button
                    type="button"
                    className={
                        paginaActual === 'catalogo'
                            ? 'navbar-link navbar-link-active'
                            : 'navbar-link'
                    }
                    onClick={onCatalogo}
                    aria-current={
                        paginaActual === 'catalogo' ? 'page' : undefined
                    }
                >
                    Catálogo
                </button>

                {isAuthenticated && (
                    <button
                        type="button"
                        className="navbar-link"
                        onClick={onAbrirPedidos}
                    >
                        Mis pedidos
                    </button>
                )}

                <button
                    type="button"
                    className="navbar-carrito"
                    onClick={onAbrirCarrito}
                    aria-label={`Abrir carrito, ${cantidadCarrito} unidades`}
                >
                    <span aria-hidden="true">🛒</span>
                    Carrito
                    <span className="navbar-carrito-contador">
                        {cantidadCarrito}
                    </span>
                </button>

                {isAuthenticated ? (
                    <div className="navbar-usuario">
                        {nombreUsuario && (
                            <span className="navbar-user">
                                {nombreUsuario}
                            </span>
                        )}

                        <button type="button" onClick={onLogout}>
                            Cerrar sesión
                        </button>
                    </div>
                ) : (
                    <button type="button" onClick={onLogin}>
                        Iniciar sesión
                    </button>
                )}
            </div>
        </nav>
    );
}
