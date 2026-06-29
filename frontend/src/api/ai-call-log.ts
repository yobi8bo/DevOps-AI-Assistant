import { http, type ApiResponse } from './http';

export interface PageResponse<T> {
  records: T[];
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
}

export interface AiCallLogItem {
  id: number;
  requestId: string;
  userId?: number;
  sessionId?: number;
  modelConfigId?: number;
  provider?: string;
  modelName?: string;
  promptTokens?: number;
  completionTokens?: number;
  totalTokens?: number;
  latencyMs?: number;
  success: boolean;
  errorCode?: string;
  errorMessage?: string;
  createdAt: string;
}

export interface AiCallLogQuery {
  keyword?: string;
  success?: boolean;
  modelConfigId?: number;
  sessionId?: number;
  userId?: number;
  startTime?: string;
  endTime?: string;
  pageNum: number;
  pageSize: number;
}

export async function fetchAiCallLogs(query: AiCallLogQuery) {
  const { data } = await http.get<ApiResponse<PageResponse<AiCallLogItem>>>('/ai-call-logs', {
    params: query,
  });
  return data.data;
}
