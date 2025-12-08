import { UserModel, CreateUserData, User } from '../models/User';
import { hashPassword, comparePassword, generateAccessToken, generateRefreshToken, verifyRefreshToken } from '@lambrk/shared';
import { ValidationError, UnauthorizedError, ConflictError } from '@lambrk/shared';

export class AuthService {
  private userModel: UserModel;

  constructor() {
    this.userModel = new UserModel();
  }

  async signup(data: {
    username: string;
    email: string;
    password: string;
  }): Promise<{ user: Omit<User, 'password'>; accessToken: string; refreshToken: string }> {
    const existingUserByEmail = await this.userModel.findByEmail(data.email);
    if (existingUserByEmail) {
      throw new ConflictError('Email already registered');
    }

    const existingUserByUsername = await this.userModel.findByUsername(data.username);
    if (existingUserByUsername) {
      throw new ConflictError('Username already taken');
    }

    const hashedPassword = await hashPassword(data.password);

    const userData: CreateUserData = {
      username: data.username,
      email: data.email,
      password: hashedPassword,
    };

    const user = await this.userModel.create(userData);

    const accessToken = generateAccessToken({
      userId: user.id,
      email: user.email,
      username: user.username,
    });

    const refreshToken = generateRefreshToken({
      userId: user.id,
      email: user.email,
      username: user.username,
    });

    const { password, ...userWithoutPassword } = user;

    return {
      user: userWithoutPassword,
      accessToken,
      refreshToken,
    };
  }

  async signin(data: {
    email?: string;
    username?: string;
    password: string;
  }): Promise<{ user: Omit<User, 'password'>; accessToken: string; refreshToken: string }> {
    let user: User | null = null;

    if (data.email) {
      user = await this.userModel.findByEmail(data.email);
    } else if (data.username) {
      user = await this.userModel.findByUsername(data.username);
    } else {
      throw new ValidationError('Email or username is required');
    }

    if (!user) {
      throw new UnauthorizedError('Invalid credentials');
    }

    if (!user.password) {
      throw new UnauthorizedError('Please sign in with Google');
    }

    const isPasswordValid = await comparePassword(data.password, user.password);
    if (!isPasswordValid) {
      throw new UnauthorizedError('Invalid credentials');
    }

    const accessToken = generateAccessToken({
      userId: user.id,
      email: user.email,
      username: user.username,
    });

    const refreshToken = generateRefreshToken({
      userId: user.id,
      email: user.email,
      username: user.username,
    });

    const { password, ...userWithoutPassword } = user;

    return {
      user: userWithoutPassword,
      accessToken,
      refreshToken,
    };
  }

  async googleAuth(profile: {
    id: string;
    emails: Array<{ value: string }>;
    name: { givenName?: string; familyName?: string };
    photos?: Array<{ value: string }>;
  }): Promise<{ user: Omit<User, 'password'>; accessToken: string; refreshToken: string }> {
    const email = profile.emails[0]?.value;
    if (!email) {
      throw new ValidationError('Email not provided by Google');
    }

    let user = await this.userModel.findByGoogleId(profile.id);

    if (!user) {
      user = await this.userModel.findByEmail(email);
      if (user) {
        user = await this.userModel.update(user.id, {
          googleId: profile.id,
          firstName: profile.name.givenName,
          lastName: profile.name.familyName,
          avatar: profile.photos?.[0]?.value,
        });
      } else {
        const username = email.split('@')[0] + '_' + Date.now();
        const userData: CreateUserData = {
          username,
          email,
          googleId: profile.id,
          firstName: profile.name.givenName,
          lastName: profile.name.familyName,
          avatar: profile.photos?.[0]?.value,
        };
        user = await this.userModel.create(userData);
      }
    } else {
      if (profile.name.givenName || profile.name.familyName || profile.photos?.[0]?.value) {
        user = await this.userModel.update(user.id, {
          firstName: profile.name.givenName,
          lastName: profile.name.familyName,
          avatar: profile.photos?.[0]?.value,
        });
      }
    }

    const accessToken = generateAccessToken({
      userId: user.id,
      email: user.email,
      username: user.username,
    });

    const refreshToken = generateRefreshToken({
      userId: user.id,
      email: user.email,
      username: user.username,
    });

    const { password, ...userWithoutPassword } = user;

    return {
      user: userWithoutPassword,
      accessToken,
      refreshToken,
    };
  }

  async refreshToken(refreshToken: string): Promise<{ accessToken: string }> {
    const decoded = verifyRefreshToken(refreshToken);

    const user = await this.userModel.findById(decoded.userId);
    if (!user) {
      throw new UnauthorizedError('User not found');
    }

    const accessToken = generateAccessToken({
      userId: user.id,
      email: user.email,
      username: user.username,
    });

    return { accessToken };
  }

  async getProfile(userId: string): Promise<Omit<User, 'password'>> {
    const user = await this.userModel.findById(userId);
    if (!user) {
      throw new UnauthorizedError('User not found');
    }

    const { password, ...userWithoutPassword } = user;
    return userWithoutPassword;
  }
}

