import { http, type ApiResponse } from './http';

export interface PageResponse<T> {
  records: T[];
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
}

export interface ModelConfig {
  id: number;
  provider: string;
  apiStyle: string;
  modelName: string;
  apiBaseUrl: string;
  maxTokens: number;
  temperature: number;
  timeoutSeconds: number;
  defaultModel: boolean;
  enabled: boolean;
  hasApiKey: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ModelConfigPayload {
  provider: string;
  apiStyle: string;
  modelName: string;
  apiBaseUrl: string;
  apiKey?: string;
  maxTokens: number;
  temperature: number;
  timeoutSeconds: number;
  defaultModel: boolean;
  status: number;
}

export interface ModelConfigQuery {
  keyword?: string;
  status?: number;
  pageNum: number;
  pageSize: number;
}

export interface TestConnectionResponse {
  success: boolean;
  message: string;
  latencyMs: number;
}

export async function fetchModelConfigs(query: ModelConfigQuery) {
  const { data } = await http.get<ApiResponse<PageResponse<ModelConfig>>>('/model-configs', {
    params: query,
  });
  return data.data;
}

export async function createModelConfig(payload: ModelConfigPayload) {
  const { data } = await http.post<ApiResponse<ModelConfig>>('/model-configs', payload);
  return data.data;
}

export async function updateModelConfig(id: number, payload: ModelConfigPayload) {
  const { data } = await http.put<ApiResponse<ModelConfig>>(`/model-configs/${id}`, payload);
  return data.data;
}

export async function updateModelConfigStatus(id: number, status: number) {
  await http.patch<ApiResponse<boolean>>(`/model-configs/${id}/status`, { status });
}

export async function setDefaultModelConfig(id: number) {
  await http.post<ApiResponse<boolean>>(`/model-configs/${id}/default`);
}

export async function testModelConfig(id: number, apiKey?: string) {
  const { data } = await http.post<ApiResponse<TestConnectionResponse>>(`/model-configs/${id}/test`, { apiKey });
  return data.data;
}

export async function deleteModelConfig(id: number) {
  await http.delete<ApiResponse<boolean>>(`/model-configs/${id}`);
}
