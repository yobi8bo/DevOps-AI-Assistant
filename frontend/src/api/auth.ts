import { http, type ApiResponse } from './http';

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  email?: string;
  roles: string[];
  permissions: string[];
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userInfo: UserInfo;
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  const response = await http.post<ApiResponse<LoginResponse>>('/auth/login', { username, password });
  return response.data.data;
}

export async function fetchCurrentUser(): Promise<UserInfo> {
  const response = await http.get<ApiResponse<UserInfo>>('/auth/me');
  return response.data.data;
}

export async function logout(): Promise<boolean> {
  const response = await http.post<ApiResponse<boolean>>('/auth/logout');
  return response.data.data;
}

export async function changePassword(oldPassword: string, newPassword: string): Promise<boolean> {
  const response = await http.post<ApiResponse<boolean>>('/auth/change-password', { oldPassword, newPassword });
  return response.data.data;
}
