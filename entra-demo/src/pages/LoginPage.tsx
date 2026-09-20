import { useMsal } from '@azure/msal-react';
import { InteractionStatus } from '@azure/msal-browser';

import { tokenRequest } from '../authConfig';
import LoginCard from '../components/LoginCard';

import '../styles/login.css';

export default function LoginPage() {
    const { instance, inProgress } = useMsal();

    const ocupado = inProgress !== InteractionStatus.None;

    async function iniciarSesion() {
        try {
            const result = await instance.loginPopup({
                ...tokenRequest,
                prompt: 'select_account',
            });

            instance.setActiveAccount(result.account);
        } catch (error) {
            console.error('Error durante el inicio de sesión:', error);
        }
    }

    return (
        <main className="login-page">
            <div className="login-background-decoration decoration-one"></div>
            <div className="login-background-decoration decoration-two"></div>

            <LoginCard
                onLogin={() => void iniciarSesion()}
                disabled={ocupado}
            />

            <footer className="login-footer">
                <span>Pedidos360</span>
                <span>•</span>
                <span>Desarrollo Cloud Native</span>
            </footer>
        </main>
    );
}