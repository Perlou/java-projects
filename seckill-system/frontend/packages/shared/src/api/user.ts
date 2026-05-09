import request from "./request";
import type { User, PageResult } from "../types";

export const userApi = {
  // 用户注册
  register(username: string, password: string) {
    return request.post<User>(
      `/users/register?username=${username}&password=${password}`
    );
  },

  // 用户登录
  login(username: string, password: string) {
    return request.post<User>(
      `/users/login?username=${username}&password=${password}`
    );
  },

  // 获取用户信息
  getById(id: number) {
    return request.get<User>(`/users/${id}`);
  },

  // 获取所有用户
  getAll() {
    return request.get<User[]>("/users");
  },
};
