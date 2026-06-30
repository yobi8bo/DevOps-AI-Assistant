import { http, type ApiResponse } from './http';
import type { PageResponse } from './user';

export interface RoleSummary {
  id: number;
  roleCode: string;
  roleName: string;
  description?: string;
  enabled: boolean;
  permissions: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface RoleDetail extends RoleSummary {
  permissionIds: number[];
}

export interface PermissionSummary {
  id: number;
  permissionCode: string;
  permissionName: string;
  description?: string;
}

export interface RoleQuery {
  keyword?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface RolePayload {
  roleCode?: string;
  roleName?: string;
  description?: string;
  status?: number;
  permissionIds?: number[];
}

export async function fetchRoles(query: RoleQuery): Promise<PageResponse<RoleSummary>> {
  const response = await http.get<ApiResponse<PageResponse<RoleSummary>>>('/roles', {
    params: query,
  });
  return response.data.data;
}

export async function fetchRole(id: number): Promise<RoleDetail> {
  const response = await http.get<ApiResponse<RoleDetail>>(`/roles/${id}`);
  return response.data.data;
}

export async function createRole(payload: RolePayload): Promise<RoleDetail> {
  const response = await http.post<ApiResponse<RoleDetail>>('/roles', payload);
  return response.data.data;
}

export async function updateRole(id: number, payload: RolePayload): Promise<RoleDetail> {
  const response = await http.put<ApiResponse<RoleDetail>>(`/roles/${id}`, payload);
  return response.data.data;
}

export async function updateRoleStatus(id: number, status: number): Promise<boolean> {
  const response = await http.patch<ApiResponse<boolean>>(`/roles/${id}/status`, { status });
  return response.data.data;
}

export async function updateRolePermissions(id: number, permissionIds: number[]): Promise<boolean> {
  const response = await http.put<ApiResponse<boolean>>(`/roles/${id}/permissions`, { permissionIds });
  return response.data.data;
}

export async function fetchPermissions(keyword?: string): Promise<PermissionSummary[]> {
  const response = await http.get<ApiResponse<PermissionSummary[]>>('/permissions', {
    params: keyword ? { keyword } : undefined,
  });
  return response.data.data;
}
