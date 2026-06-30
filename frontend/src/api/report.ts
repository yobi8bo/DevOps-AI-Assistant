import { http, type ApiResponse } from './http';

export interface ReportSummary {
  id: number;
  sessionId: number;
  title: string;
  format: string;
  createdBy?: number;
  createdAt: string;
}

export interface ReportDetail extends ReportSummary {
  content: string;
}

export async function createReportFromSession(sessionId: number): Promise<ReportDetail> {
  const response = await http.post<ApiResponse<ReportDetail>>(`/reports/from-session/${sessionId}`);
  return response.data.data;
}

export async function fetchReport(id: number): Promise<ReportDetail> {
  const response = await http.get<ApiResponse<ReportDetail>>(`/reports/${id}`);
  return response.data.data;
}

export async function fetchReports(sessionId?: number): Promise<ReportSummary[]> {
  const response = await http.get<ApiResponse<ReportSummary[]>>('/reports', {
    params: { sessionId },
  });
  return response.data.data;
}

export async function deleteReport(id: number): Promise<boolean> {
  const response = await http.delete<ApiResponse<boolean>>(`/reports/${id}`);
  return response.data.data;
}
