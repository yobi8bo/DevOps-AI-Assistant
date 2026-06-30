import { http, type ApiResponse } from './http';

export interface PageResponse<T> {
  records: T[];
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
}

export interface KnowledgeSummary {
  id: number;
  title: string;
  category?: string;
  tags: string[];
  contentType: string;
  sourceType: string;
  sourceRef?: string;
  version: string;
  enabled: boolean;
  createdBy?: number;
  updatedBy?: number;
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgeDetail extends KnowledgeSummary {
  content: string;
}

export interface KnowledgePayload {
  title: string;
  category?: string;
  tags?: string[];
  content: string;
  contentType?: string;
  sourceType?: string;
  sourceRef?: string;
  version?: string;
  status?: number;
}

export interface KnowledgeQuery {
  keyword?: string;
  category?: string;
  tag?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export async function fetchKnowledgeList(query: KnowledgeQuery): Promise<PageResponse<KnowledgeSummary>> {
  const response = await http.get<ApiResponse<PageResponse<KnowledgeSummary>>>('/knowledge', {
    params: query,
  });
  return response.data.data;
}

export async function fetchKnowledge(id: number): Promise<KnowledgeDetail> {
  const response = await http.get<ApiResponse<KnowledgeDetail>>(`/knowledge/${id}`);
  return response.data.data;
}

export async function createKnowledge(payload: KnowledgePayload): Promise<KnowledgeDetail> {
  const response = await http.post<ApiResponse<KnowledgeDetail>>('/knowledge', payload);
  return response.data.data;
}

export async function updateKnowledge(id: number, payload: KnowledgePayload): Promise<KnowledgeDetail> {
  const response = await http.put<ApiResponse<KnowledgeDetail>>(`/knowledge/${id}`, payload);
  return response.data.data;
}

export async function deleteKnowledge(id: number): Promise<boolean> {
  const response = await http.delete<ApiResponse<boolean>>(`/knowledge/${id}`);
  return response.data.data;
}

export async function updateKnowledgeStatus(id: number, status: number): Promise<boolean> {
  const response = await http.patch<ApiResponse<boolean>>(`/knowledge/${id}/status`, { status });
  return response.data.data;
}

export async function createKnowledgeFromCase(caseId: number, payload?: Partial<KnowledgePayload>): Promise<KnowledgeDetail> {
  const response = await http.post<ApiResponse<KnowledgeDetail>>(`/knowledge/from-case/${caseId}`, payload || {});
  return response.data.data;
}
