// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** hello GET /api/hello */
export async function helloUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseString_>('/api/hello', {
    method: 'GET',
    ...(options || {}),
  })
}
