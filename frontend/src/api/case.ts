import { http, type ApiResponse } from './http';

export interface PageResponse<T> {
  records: T[];
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
}

export type CaseStatus = 'DRAFT' | 'PENDING_REVIEW' | 'PUBLISHED' | 'REJECTED' | 'OFFLINE';

export interface CaseSummary {
  id: number;
  sourceSessionId?: number;
  title: string;
  category?: string;
  environment?: string;
  status: CaseStatus;
  tags: string[];
  createdBy?: number;
  reviewedBy?: number;
  reviewedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CaseDetail extends CaseSummary {
  symptom?: string;
  logContent?: string;
  causeAnalysis?: string;
  solution?: string;
  prevention?: string;
  commands?: unknown;
}

export interface CasePayload {
  sourceSessionId?: number;
  title: string;
  category?: string;
  environment?: string;
  symptom?: string;
  logContent?: string;
  causeAnalysis?: string;
  solution?: string;
  prevention?: string;
  commands?: unknown;
  tags?: string[];
  status?: CaseStatus;
}

export interface CaseQuery {
  keyword?: string;
  category?: string;
  status?: CaseStatus;
  tag?: string;
  pageNum?: number;
  pageSize?: number;
}

export async function fetchCases(query: CaseQuery): Promise<PageResponse<CaseSummary>> {
  const response = await http.get<ApiResponse<PageResponse<CaseSummary>>>('/cases', {
    params: query,
  });
  return response.data.data;
}

export async function fetchCase(id: number): Promise<CaseDetail> {
  const response = await http.get<ApiResponse<CaseDetail>>(`/cases/${id}`);
  return response.data.data;
}

export async function createCase(payload: CasePayload): Promise<CaseDetail> {
  const response = await http.post<ApiResponse<CaseDetail>>('/cases', payload);
  return response.data.data;
}

export async function updateCase(id: number, payload: CasePayload): Promise<CaseDetail> {
  const response = await http.put<ApiResponse<CaseDetail>>(`/cases/${id}`, payload);
  return response.data.data;
}

export async function deleteCase(id: number): Promise<boolean> {
  const response = await http.delete<ApiResponse<boolean>>(`/cases/${id}`);
  return response.data.data;
}

export async function createCaseFromSession(sessionId: number, payload?: Partial<CasePayload>): Promise<CaseDetail> {
  const response = await http.post<ApiResponse<CaseDetail>>(`/cases/from-session/${sessionId}`, payload || {});
  return response.data.data;
}

export async function updateCaseStatus(id: number, status: CaseStatus): Promise<boolean> {
  const response = await http.patch<ApiResponse<boolean>>(`/cases/${id}/status`, { status });
  return response.data.data;
}
