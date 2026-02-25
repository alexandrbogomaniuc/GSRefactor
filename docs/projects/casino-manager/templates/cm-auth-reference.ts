/*
  Reference-only auth logic for CM phase-1.
  Not wired to a specific framework.
*/

type User = {
  username: string;
  passwordHash: string;
  roleSet: string[];
  status: "ACTIVE" | "LOCKED";
  mustChangePassword: boolean;
  failedAttempts: number;
  lockedUntil?: Date;
};

type LoginResult = {
  accessToken: string;
  refreshToken: string;
  mustChangePassword: boolean;
  roles: string[];
};

interface UserRepo {
  countUsers(): Promise<number>;
  getByUsername(username: string): Promise<User | null>;
  upsert(user: User): Promise<void>;
}

interface TokenRepo {
  saveRefreshTokenHash(username: string, tokenHash: string, expiresAt: Date): Promise<void>;
  revokeAll(username: string): Promise<void>;
}

interface CryptoApi {
  hashPassword(raw: string): Promise<string>; // Argon2id
  verifyPassword(hash: string, raw: string): Promise<boolean>;
  hashToken(raw: string): Promise<string>;
  randomToken(): string;
}

export class CmAuthService {
  constructor(
    private readonly users: UserRepo,
    private readonly tokens: TokenRepo,
    private readonly crypto: CryptoApi
  ) {}

  async bootstrapDefaultRoot(): Promise<void> {
    const count = await this.users.countUsers();
    if (count > 0) return;

    const passwordHash = await this.crypto.hashPassword("root");
    await this.users.upsert({
      username: "root",
      passwordHash,
      roleSet: ["SUPER_ADMIN"],
      status: "ACTIVE",
      mustChangePassword: true,
      failedAttempts: 0,
    });
  }

  async login(username: string, password: string, now: Date): Promise<LoginResult> {
    const user = await this.users.getByUsername(username);
    if (!user) throw new Error("INVALID_CREDENTIALS");
    if (user.status !== "ACTIVE") throw new Error("ACCOUNT_DISABLED");
    if (user.lockedUntil && user.lockedUntil > now) throw new Error("ACCOUNT_LOCKED");

    const ok = await this.crypto.verifyPassword(user.passwordHash, password);
    if (!ok) {
      user.failedAttempts += 1;
      if (user.failedAttempts >= 5) {
        user.lockedUntil = new Date(now.getTime() + 15 * 60 * 1000);
      }
      await this.users.upsert(user);
      throw new Error("INVALID_CREDENTIALS");
    }

    user.failedAttempts = 0;
    user.lockedUntil = undefined;
    await this.users.upsert(user);

    const accessToken = this.crypto.randomToken();
    const refreshToken = this.crypto.randomToken();
    const refreshHash = await this.crypto.hashToken(refreshToken);
    const refreshExpiry = new Date(now.getTime() + 8 * 60 * 60 * 1000);
    await this.tokens.saveRefreshTokenHash(user.username, refreshHash, refreshExpiry);

    return {
      accessToken,
      refreshToken,
      mustChangePassword: user.mustChangePassword,
      roles: user.roleSet,
    };
  }

  async changePassword(username: string, oldPassword: string, newPassword: string): Promise<void> {
    const user = await this.users.getByUsername(username);
    if (!user) throw new Error("NOT_FOUND");

    const ok = await this.crypto.verifyPassword(user.passwordHash, oldPassword);
    if (!ok) throw new Error("INVALID_CREDENTIALS");

    const passwordHash = await this.crypto.hashPassword(newPassword);
    user.passwordHash = passwordHash;
    user.mustChangePassword = false;
    await this.users.upsert(user);
    await this.tokens.revokeAll(username);
  }
}
