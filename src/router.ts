import type { APIGatewayProxyEvent } from 'aws-lambda';
import { Http, type ApiResponse } from './http.js';
import { Services } from './controller.js';

export interface RouteHandlerContext {
  event: APIGatewayProxyEvent;
  pathParameters: Record<string, string>;
}

export type RouteHandler = (context: RouteHandlerContext, services: Services) => Promise<ApiResponse>;

export interface Route {
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  path: RegExp;
  handler: RouteHandler;
}

interface FunctionUrlEventShape {
  rawPath?: string;
  requestContext?: { http?: { method?: string } };
}

export class Router {
  private routes: Route[] = [];
  private services: Services;

  public constructor(services: Services) {
    this.services = services
  }  

  public async handle(event: APIGatewayProxyEvent): Promise<ApiResponse> {
    const { method, path } = this.extractMethodAndPath(event);

    for (const route of this.routes) {
      if (route.method !== method) continue;
      const match = path.match(route.path);
      if (!match) continue;
      return route.handler({
        event,
        pathParameters: (match.groups ?? {}) as Record<string, string>,
      }, this.services);
    }

    return Http.response(404, { error: 'Not found' });
  }

  public addPost(path: RegExp, handler: RouteHandler) {
    this.routes.push({
      method: 'POST',
      path: path,
      handler: handler
    })
  }

  public addGet(path: RegExp, handler: RouteHandler) {
    this.routes.push({
      method: 'GET',
      path: path,
      handler: handler
    })
  }

  private extractMethodAndPath(event: APIGatewayProxyEvent): { method: string; path: string } {
    const functionUrl = event as unknown as FunctionUrlEventShape;
    const method = (event.httpMethod ?? functionUrl.requestContext?.http?.method ?? '').toUpperCase();
    const path = event.path ?? functionUrl.rawPath ?? '';
    return { method, path };
  }
}
