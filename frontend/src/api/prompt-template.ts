import { http, type ApiResponse } from './http';

export interface PageResponse<T> {
  records: T[];
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
}

export interface PromptTemplateSummary {
  id: number;
  name: string;
  category?: string;
  version: string;
  defaultTemplate: boolean;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PromptTemplateDetail extends PromptTemplateSummary {
  content: string;
}

export interface PromptTemplatePayload {
  name: string;
  category?: string;
  content: string;
  version: string;
  defaultTemplate: boolean;
  status: number;
}

export interface PromptTemplateQuery {
  keyword?: string;
  category?: string;
  status?: number;
  pageNum: number;
  pageSize: number;
}

export async function fetchPromptTemplates(query: PromptTemplateQuery) {
  const { data } = await http.get<ApiResponse<PageResponse<PromptTemplateSummary>>>('/prompt-templates', {
    params: query,
  });
  return data.data;
}

export async function fetchPromptTemplate(id: number) {
  const { data } = await http.get<ApiResponse<PromptTemplateDetail>>(`/prompt-templates/${id}`);
  return data.data;
}

export async function createPromptTemplate(payload: PromptTemplatePayload) {
  const { data } = await http.post<ApiResponse<PromptTemplateDetail>>('/prompt-templates', payload);
  return data.data;
}

export async function updatePromptTemplate(id: number, payload: PromptTemplatePayload) {
  const { data } = await http.put<ApiResponse<PromptTemplateDetail>>(`/prompt-templates/${id}`, payload);
  return data.data;
}

export async function updatePromptTemplateStatus(id: number, status: number) {
  await http.patch<ApiResponse<boolean>>(`/prompt-templates/${id}/status`, { status });
}

export async function setDefaultPromptTemplate(id: number) {
  await http.post<ApiResponse<boolean>>(`/prompt-templates/${id}/default`);
}

export async function deletePromptTemplate(id: number) {
  await http.delete<ApiResponse<boolean>>(`/prompt-templates/${id}`);
}
