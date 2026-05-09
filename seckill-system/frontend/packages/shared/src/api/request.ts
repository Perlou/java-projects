import axios, { type AxiosInstance, type AxiosResponse } from "axios";
import type { ApiResult } from "../types";

const BASE_URL = "/api";

/**
 * 统一 Axios 请求封装
 * 支持 Vue 和 React 复用
 */
class Request {
  private instance: AxiosInstance;

  constructor(baseUrl: string = BASE_URL) {
    this.instance = axios.create({
      baseURL: baseUrl,
      timeout: 10000,
      headers: {
        "Content-Type": "application/json",
      },
    });

    // 请求拦截器
    this.instance.interceptors.request.use(
      (config) => {
        // 可以在这里添加 token
        const token = localStorage.getItem("token");
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // 响应拦截器
    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResult>) => {
        return response;
      },
      (error) => {
        // 统一错误处理
        if (error.response) {
          const { status } = error.response;
          if (status === 401) {
            // 未授权，跳转登录
            localStorage.removeItem("token");
          } else if (status === 403) {
            console.error("没有权限");
          } else if (status === 500) {
            console.error("服务器错误");
          }
        }
        return Promise.reject(error);
      }
    );
  }

  async get<T>(
    url: string,
    params?: Record<string, any>
  ): Promise<ApiResult<T>> {
    const response = await this.instance.get<ApiResult<T>>(url, { params });
    return response.data;
  }

  async post<T>(url: string, data?: any): Promise<ApiResult<T>> {
    const response = await this.instance.post<ApiResult<T>>(url, data);
    return response.data;
  }

  async put<T>(url: string, data?: any): Promise<ApiResult<T>> {
    const response = await this.instance.put<ApiResult<T>>(url, data);
    return response.data;
  }

  async delete<T>(url: string): Promise<ApiResult<T>> {
    const response = await this.instance.delete<ApiResult<T>>(url);
    return response.data;
  }
}

export const request = new Request();
export default request;
