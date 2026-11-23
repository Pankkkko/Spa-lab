
export type AdminSection =
    | 'principal'
    | 'productos'
    | 'reportes'
    | 'auditoria'
    | 'pedidos'
    | 'clientes';

interface AdminNavbarProps {
    paginaActual: AdminSection;
    onCambiarPagina: (pagina: AdminSection) => void;
    onLogout: () => void;
}

const SECCIONES: { id: AdminSection; nombre: string }[] = [
    { id: 'principal', nombre: 'Principal' },
    { id: 'productos', nombre: 'Productos' },
    { id: 'reportes', nombre: 'Reportes' },
    { id: 'auditoria', nombre: 'Auditoría' },
    { id: 'pedidos', nombre: 'Pedidos' },
    { id: 'clientes', nombre: 'Clientes' },
];

export default function AdminNavbar({
    paginaActual,
    onCambiarPagina,
    onLogout,
}: AdminNavbarProps) {
    return (
        <nav className="admin-navbar" aria-label="Navegación administrativa">
            <div className="admin-navbar-brand">
                <div className="admin-navbar-logo">P360</div>
                <span>Pedidos360</span>
                <small>ADMIN</small>
            </div>

            <div className="admin-navbar-actions">
                {SECCIONES.map((seccion) => (
                    <button
                        key={seccion.id}
                        type="button"
                        className={
                            paginaActual === seccion.id
                                ? 'admin-navbar-link admin-navbar-link-active'
                                : 'admin-navbar-link'
                        }
                        onClick={() => onCambiarPagina(seccion.id)}
                        aria-current={
                            paginaActual === seccion.id ? 'page' : undefined
                        }
                    >
                        {seccion.nombre}
                    </button>
                ))}

                <button
                    type="button"
                    className="admin-navbar-logout"
                    onClick={onLogout}
                >
                    Cerrar sesión
                </button>
            </div>
        </nav>
    );
}
