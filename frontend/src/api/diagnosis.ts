import { http, type ApiResponse } from './http';

export interface PageResponse<T> {
  records: T[];
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
}

export interface AnalyzeRequest {
  title: string;
  category?: string;
  environment?: string;
  isProduction?: boolean;
  urgencyLevel?: string;
  description?: string;
  logContent?: string;
  commandOutput?: string;
  modelConfigId?: number;
}

export interface CommandSuggestion {
  command: string;
  description: string;
  riskLevel: string;
  warning: string;
}

export interface AnalyzeResponse {
  sessionId: number;
  messageId: number;
  resultId: number;
  summary: string;
  possibleCauses: string[];
  checkSteps: string[];
  fixSteps: string[];
  commands: CommandSuggestion[];
  riskLevel: string;
  riskWarnings: string[];
  needRestart: boolean;
  dataRisk: boolean;
  prevention: string;
  needMoreInfo: string[];
}

export interface SessionSummary {
  id: number;
  title: string;
  category?: string;
  environment?: string;
  isProduction: boolean;
  urgencyLevel?: string;
  status: string;
  riskLevel: string;
  summary?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MessageItem {
  id: number;
  role: 'user' | 'assistant' | 'system';
  content: string;
  createdAt: string;
}

export interface ResultItem {
  id: number;
  summary: string;
  riskLevel: string;
  resultJson: string;
  modelConfigId?: number;
  promptTemplateId?: number;
  promptVersion?: string;
  createdAt: string;
}

export interface SessionDetail {
  id: number;
  title: string;
  category?: string;
  environment?: string;
  isProduction: boolean;
  urgencyLevel?: string;
  status: string;
  messages: MessageItem[];
  latestResult?: ResultItem | null;
  createdAt: string;
  updatedAt: string;
}

export interface SessionQuery {
  keyword?: string;
  category?: string;
  status?: string;
  isProduction?: boolean;
  pageNum?: number;
  pageSize?: number;
}

export async function analyzeDiagnosis(payload: AnalyzeRequest): Promise<AnalyzeResponse> {
  const response = await http.post<ApiResponse<AnalyzeResponse>>('/diagnosis/analyze', payload);
  return response.data.data;
}

export async function fetchDiagnosisSessions(query: SessionQuery): Promise<PageResponse<SessionSummary>> {
  const response = await http.get<ApiResponse<PageResponse<SessionSummary>>>('/diagnosis/sessions', {
    params: query,
  });
  return response.data.data;
}

export async function fetchDiagnosisSession(id: number): Promise<SessionDetail> {
  const response = await http.get<ApiResponse<SessionDetail>>(`/diagnosis/sessions/${id}`);
  return response.data.data;
}

export async function updateDiagnosisSessionStatus(id: number, status: string): Promise<boolean> {
  const response = await http.patch<ApiResponse<boolean>>(`/diagnosis/sessions/${id}/status`, { status });
  return response.data.data;
}

export async function deleteDiagnosisSession(id: number): Promise<boolean> {
  const response = await http.delete<ApiResponse<boolean>>(`/diagnosis/sessions/${id}`);
  return response.data.data;
}
