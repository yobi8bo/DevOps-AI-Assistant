import { http, type ApiResponse } from './http';
import type { PageResponse } from './user';

export interface OperationLogItem {
  id: number;
  userId?: number;
  username?: string;
  module?: string;
  action?: string;
  targetId?: number;
  requestMethod?: string;
  requestUri?: string;
  ipAddress?: string;
  userAgent?: string;
  success: boolean;
  errorMessage?: string;
  createdAt: string;
}

export interface OperationLogQuery {
  keyword?: string;
  module?: string;
  action?: string;
  success?: boolean;
  pageNum?: number;
  pageSize?: number;
}

export async function fetchOperationLogs(query: OperationLogQuery): Promise<PageResponse<OperationLogItem>> {
  const response = await http.get<ApiResponse<PageResponse<OperationLogItem>>>('/operation-logs', {
    params: query,
  });
  return response.data.data;
}
