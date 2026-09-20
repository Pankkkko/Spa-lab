interface MicrosoftLoginButtonProps {
    onClick: () => void;
    disabled?: boolean;
}

export default function MicrosoftLoginButton({
    onClick,
    disabled = false,
}: MicrosoftLoginButtonProps) {
    return (
        <button
            type="button"
            className="microsoft-login-button"
            onClick={onClick}
            disabled={disabled}
        >
            <span className="microsoft-logo">
                <span></span>
                <span></span>
                <span></span>
                <span></span>
            </span>

            <span>Iniciar sesión con Microsoft</span>
        </button>
    );
}