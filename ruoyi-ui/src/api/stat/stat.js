import request from '@/utils/request'

export function listStatTypes() {
  return request({
    url: '/stat/tree',
    method: 'get'
  })
}