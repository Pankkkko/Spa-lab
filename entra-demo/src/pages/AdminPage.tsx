import { useState } from 'react';

import AdminNavbar, {
    type AdminSection,
} from '../components/AdminNavbar';

import AdminPrincipal from '../components/admin/AdminPrincipal';
import AdminProductos from '../components/admin/AdminProductos';
import AdminReportes from '../components/admin/AdminReportes';
import AdminAuditoria from '../components/admin/AdminAuditoria';
import AdminPedidos from '../components/admin/AdminPedidos';
import AdminClientes from '../components/admin/AdminClientes';

import '../styles/admin.css';
import '../styles/admin-navbar.css';

interface AdminPageProps {
    onLogout: () => void;
}

function AdminPage({ onLogout }: AdminPageProps) {
    const [seccionActual, setSeccionActual] =
        useState<AdminSection>('principal');

    return (
        <>
            <AdminNavbar
                paginaActual={seccionActual}
                onCambiarPagina={setSeccionActual}
                onLogout={onLogout}
            />

            <main className="admin-page">
                {seccionActual === 'principal' && <AdminPrincipal />}
                {seccionActual === 'productos' && <AdminProductos />}
                {seccionActual === 'reportes' && <AdminReportes />}
                {seccionActual === 'auditoria' && <AdminAuditoria />}
                {seccionActual === 'pedidos' && <AdminPedidos />}
                {seccionActual === 'clientes' && <AdminClientes />}
            </main>
        </>
    );
}

export default AdminPage;
