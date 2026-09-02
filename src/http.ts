import type { APIGatewayProxyEvent } from 'aws-lambda';

export interface ApiResponse {
  statusCode: number;
  headers: Record<string, string>;
  body: string;
}

export class Http {
  public static parseBody(event: APIGatewayProxyEvent): Record<string, unknown> {
    if (!event.body) return {};
    try {
      return JSON.parse(event.body) as Record<string, unknown>;
    } catch {
      return {};
    }
  }

  public static response(
    statusCode: number,
    body: unknown,
    headers: Record<string, string> = {}
  ): ApiResponse {
    return {
      statusCode,
      headers: { 'content-type': 'application/json', ...headers },
      body: JSON.stringify(body),
    };
  }
}