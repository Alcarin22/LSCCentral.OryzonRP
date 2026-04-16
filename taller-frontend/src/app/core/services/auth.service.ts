import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) {}

  loginWithDiscord(): void {
    const clientId = environment.discordClientId;
    const redirectUri = encodeURIComponent(environment.discordRedirectUri);
    const scope = encodeURIComponent('identify guilds.members.read');
    const responseType = 'code';

    const discordAuthUrl =
      `https://discord.com/oauth2/authorize` +
      `?client_id=${clientId}` +
      `&response_type=${responseType}` +
      `&redirect_uri=${redirectUri}` +
      `&scope=${scope}`;

    window.location.href = discordAuthUrl;
  }

  sendCodeToBackend(code: string) {
    return this.http.post(`${environment.backendUrl}/api/auth/discord/callback`, { code });
  }
}