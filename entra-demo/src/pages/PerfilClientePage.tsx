
import { useState, type FormEvent } from 'react';
import type {
    AccountInfo,
    IPublicClientApplication,
} from '@azure/msal-browser';

import {
    crearMiPerfil,
    type ClientePerfil,
} from '../api/clienteApi';

import '../styles/perfil-cliente.css';

interface PerfilClientePageProps {
    instance: IPublicClientApplication;
    account: AccountInfo;
    onPerfilCreado: (perfil: ClientePerfil) => void;
    onLogout: () => void;
}

export default function PerfilClientePage({
    instance,
    account,
    onPerfilCreado,
    onLogout,
}: PerfilClientePageProps) {
    const [nombre, setNombre] = useState(account.name ?? '');
    const [telefono, setTelefono] = useState('');
    const [guardando, setGuardando] = useState(false);
    const [error, setError] = useState('');

    async function manejarRegistro(evento: FormEvent<HTMLFormElement>) {
        evento.preventDefault();

        if (!nombre.trim()) {
            setError('Ingresa tu nombre para continuar.');
            return;
        }

        setGuardando(true);
        setError('');

        try {
            const perfil = await crearMiPerfil(instance, account, {
                nombre: nombre.trim(),
                telefono: telefono.trim(),
            });

            onPerfilCreado(perfil);
        } catch (error) {
            console.error('Error al registrar el perfil:', error);

            setError(
                'No fue posible registrar tu perfil. ' +
                'Puede que ya exista o que el servidor no esté disponible. ' +
                'Inténtalo nuevamente.',
            );
        } finally {
            setGuardando(false);
        }
    }

    return (
        <main className="perfil-cliente-page">
            <section className="perfil-cliente-card">
                <span className="perfil-cliente-etiqueta">
                    PEDIDOS360 · MI CUENTA
                </span>

                <div className="perfil-cliente-icono" aria-hidden="true">
                    👤
                </div>

                <h1>Completa tu perfil</h1>

                <p className="perfil-cliente-descripcion">
                    Tu cuenta de Microsoft ya está conectada.
                    Solo necesitamos algunos datos para preparar tus pedidos.
                </p>

                <div className="perfil-cliente-cuenta">
                    <span>Cuenta conectada</span>
                    <strong>{account.username}</strong>
                </div>

                <form onSubmit={manejarRegistro}>
                    <label htmlFor="perfil-nombre">
                        Nombre completo
                    </label>

                    <input
                        id="perfil-nombre"
                        type="text"
                        value={nombre}
                        onChange={(evento) => setNombre(evento.target.value)}
                        maxLength={100}
                        autoComplete="name"
                        required
                        disabled={guardando}
                        placeholder="Tu nombre"
                    />

                    <label htmlFor="perfil-telefono">
                        Teléfono <span>(opcional)</span>
                    </label>

                    <input
                        id="perfil-telefono"
                        type="tel"
                        value={telefono}
                        onChange={(evento) => setTelefono(evento.target.value)}
                        maxLength={20}
                        autoComplete="tel"
                        disabled={guardando}
                        placeholder="Ej. 912345678"
                    />

                    {error && (
                        <p className="perfil-cliente-error" role="alert">
                            {error}
                        </p>
                    )}

                    <button
                        className="perfil-cliente-submit"
                        type="submit"
                        disabled={guardando}
                    >
                        {guardando
                            ? 'Guardando perfil...'
                            : 'Guardar y continuar'}
                    </button>
                </form>

                <button
                    className="perfil-cliente-logout"
                    type="button"
                    onClick={onLogout}
                    disabled={guardando}
                >
                    Cerrar sesión
                </button>
            </section>
        </main>
    );
}
