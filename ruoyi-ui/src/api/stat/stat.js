import request from '@/utils/request'

export function getStatMenuTreeData() {
  return request({
    url: '/stat/menu/tree',
    method: 'get'
  })
}