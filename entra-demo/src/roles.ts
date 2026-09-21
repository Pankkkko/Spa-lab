import type {
    AccountInfo,
    IPublicClientApplication,
} from '@azure/msal-browser';

import { tokenRequest } from './authConfig';

export type UserRole = 'Admin' | 'Cliente' | null;

interface TokenClaims {
    roles?: string[];
}

export async function obtenerRolUsuario(
    instance: IPublicClientApplication,
    account: AccountInfo,
): Promise<UserRole> {
    const result = await instance.acquireTokenSilent({
        ...tokenRequest,
        account,
    });

    const partes = result.accessToken.split('.');

    if (partes.length !== 3) {
        throw new Error('El access token no tiene formato JWT.');
    }

    const payloadBase64 = partes[1];

    const payloadJson = decodeURIComponent(
        atob(
            payloadBase64
                .replace(/-/g, '+')
                .replace(/_/g, '/'),
        )
            .split('')
            .map(
                (char) =>
                    '%' +
                    ('00' + char.charCodeAt(0).toString(16)).slice(-2),
            )
            .join(''),
    );

    const claims = JSON.parse(payloadJson) as TokenClaims;

    const roles = claims.roles ?? [];

    if (roles.includes('Admin')) {
        return 'Admin';
    }

    if (roles.includes('Cliente')) {
        return 'Cliente';
    }

    return null;
}

