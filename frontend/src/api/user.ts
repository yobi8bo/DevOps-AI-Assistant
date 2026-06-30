import { http, type ApiResponse } from './http';

export interface PageResponse<T> {
  records: T[];
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
}

export interface UserSummary {
  id: number;
  username: string;
  nickname?: string;
  email?: string;
  phone?: string;
  enabled: boolean;
  roles: string[];
  lastLoginAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserDetail extends UserSummary {
  roleIds: number[];
}

export interface UserQuery {
  keyword?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface CreateUserPayload {
  username: string;
  password: string;
  nickname?: string;
  email?: string;
  phone?: string;
  status?: number;
  roleIds?: number[];
}

export interface UpdateUserPayload {
  username?: string;
  nickname?: string;
  email?: string;
  phone?: string;
  status?: number;
}

export async function fetchUsers(query: UserQuery): Promise<PageResponse<UserSummary>> {
  const response = await http.get<ApiResponse<PageResponse<UserSummary>>>('/users', {
    params: query,
  });
  return response.data.data;
}

export async function fetchUser(id: number): Promise<UserDetail> {
  const response = await http.get<ApiResponse<UserDetail>>(`/users/${id}`);
  return response.data.data;
}

export async function createUser(payload: CreateUserPayload): Promise<UserDetail> {
  const response = await http.post<ApiResponse<UserDetail>>('/users', payload);
  return response.data.data;
}

export async function updateUser(id: number, payload: UpdateUserPayload): Promise<UserDetail> {
  const response = await http.put<ApiResponse<UserDetail>>(`/users/${id}`, payload);
  return response.data.data;
}

export async function updateUserStatus(id: number, status: number): Promise<boolean> {
  const response = await http.patch<ApiResponse<boolean>>(`/users/${id}/status`, { status });
  return response.data.data;
}

export async function resetUserPassword(id: number, password: string): Promise<boolean> {
  const response = await http.post<ApiResponse<boolean>>(`/users/${id}/reset-password`, { password });
  return response.data.data;
}

export async function updateUserRoles(id: number, roleIds: number[]): Promise<boolean> {
  const response = await http.put<ApiResponse<boolean>>(`/users/${id}/roles`, { roleIds });
  return response.data.data;
}
