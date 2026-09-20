import BrandLogo from './BrandLogo';
import MicrosoftLoginButton from './MicrosoftLoginButton';

interface LoginCardProps {
    onLogin: () => void;
    disabled?: boolean;
}

export default function LoginCard({
    onLogin,
    disabled = false,
}: LoginCardProps) {
    return (
        <section className="login-card">
            <BrandLogo />

            <div className="login-header">
                <h1>Iniciar sesión</h1>

                <p>
                    Accede a <strong>Pedidos360</strong> utilizando tu cuenta
                    institucional.
                </p>
            </div>

            <MicrosoftLoginButton
                onClick={onLogin}
                disabled={disabled}
            />

            <div className="login-divider">
                <span>o</span>
            </div>

            <div className="login-info">
                <p>
                    La autenticación se realiza de forma segura mediante
                    Microsoft Entra ID.
                </p>

                <span>
                    Solo necesitas tu cuenta de organización.
                </span>
            </div>
        </section>
    );
}