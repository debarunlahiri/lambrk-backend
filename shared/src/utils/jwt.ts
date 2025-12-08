import jwt, { SignOptions } from 'jsonwebtoken';
import type { StringValue } from 'ms';

export interface TokenPayload {
  userId: string;
  email: string;
  username?: string;
}

export const generateAccessToken = (payload: TokenPayload): string => {
  const secret = process.env.JWT_SECRET || 'default-secret';
  const expiresIn: StringValue | number = (process.env.JWT_EXPIRES_IN || '7d') as StringValue;

  const options: SignOptions = { expiresIn };
  return jwt.sign(payload, secret, options);
};

export const generateRefreshToken = (payload: TokenPayload): string => {
  const secret = process.env.JWT_REFRESH_SECRET || process.env.JWT_SECRET || 'default-secret';
  const expiresIn: StringValue | number = (process.env.JWT_REFRESH_EXPIRES_IN || '30d') as StringValue;

  const options: SignOptions = { expiresIn };
  return jwt.sign(payload, secret, options);
};

export const verifyAccessToken = (token: string): TokenPayload => {
  const secret = process.env.JWT_SECRET || 'default-secret';
  return jwt.verify(token, secret) as TokenPayload;
};

export const verifyRefreshToken = (token: string): TokenPayload => {
  const secret = process.env.JWT_REFRESH_SECRET || process.env.JWT_SECRET || 'default-secret';
  return jwt.verify(token, secret) as TokenPayload;
};

