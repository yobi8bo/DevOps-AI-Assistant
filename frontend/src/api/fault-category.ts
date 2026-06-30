import { http, type ApiResponse } from './http';
import type { PageResponse } from './user';

export interface FaultCategorySummary {
  id: number;
  categoryCode: string;
  categoryName: string;
  description?: string;
  sortOrder: number;
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface FaultCategoryOption {
  id: number;
  categoryCode: string;
  categoryName: string;
}

export interface FaultCategoryQuery {
  keyword?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface FaultCategoryPayload {
  categoryCode?: string;
  categoryName?: string;
  description?: string;
  sortOrder?: number;
  status?: number;
}

export const defaultCategoryOptions = [
  'Linux',
  'Docker',
  'Kubernetes',
  'Jenkins',
  'Nginx',
  'MySQL',
  'Redis',
  'Spring Boot',
  'Java',
  '网络',
  '权限',
  '磁盘',
  'CPU / 内存',
  '其他',
].map((value) => ({ label: value, value }));

export async function fetchFaultCategories(query: FaultCategoryQuery): Promise<PageResponse<FaultCategorySummary>> {
  const response = await http.get<ApiResponse<PageResponse<FaultCategorySummary>>>('/fault-categories', {
    params: query,
  });
  return response.data.data;
}

export async function fetchFaultCategory(id: number): Promise<FaultCategorySummary> {
  const response = await http.get<ApiResponse<FaultCategorySummary>>(`/fault-categories/${id}`);
  return response.data.data;
}

export async function fetchFaultCategoryOptions(): Promise<FaultCategoryOption[]> {
  const response = await http.get<ApiResponse<FaultCategoryOption[]>>('/fault-categories/options');
  return response.data.data;
}

export async function createFaultCategory(payload: FaultCategoryPayload): Promise<FaultCategorySummary> {
  const response = await http.post<ApiResponse<FaultCategorySummary>>('/fault-categories', payload);
  return response.data.data;
}

export async function updateFaultCategory(id: number, payload: FaultCategoryPayload): Promise<FaultCategorySummary> {
  const response = await http.put<ApiResponse<FaultCategorySummary>>(`/fault-categories/${id}`, payload);
  return response.data.data;
}

export async function updateFaultCategoryStatus(id: number, status: number): Promise<boolean> {
  const response = await http.patch<ApiResponse<boolean>>(`/fault-categories/${id}/status`, { status });
  return response.data.data;
}

export async function deleteFaultCategory(id: number): Promise<boolean> {
  const response = await http.delete<ApiResponse<boolean>>(`/fault-categories/${id}`);
  return response.data.data;
}
