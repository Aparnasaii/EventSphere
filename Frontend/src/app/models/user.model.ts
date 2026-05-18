export interface User {
  userId:        number;
  fullName:      string;
  email:         string;
  contactNumber: string;   // matches backend UserDTO field exactly
  role:          Role;
  status:        UserStatus;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  contactNumber: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  message: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export enum Role {
  ADMIN     = 'ROLE_ADMIN',
  ORGANIZER = 'ROLE_ORGANIZER',
  ATTENDEE  = 'ROLE_ATTENDEE'
}

export enum UserStatus {
  ACTIVE    = 'ACTIVE',
  PENDING   = 'PENDING',
  REJECTED  = 'REJECTED',
  SUSPENDED = 'SUSPENDED',
  INACTIVE  = 'INACTIVE'
}
