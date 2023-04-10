import request from '@/utils/request'

export function getSitePermissions(params) {
  return request({
    url: '/cms/perms/site',
    method: 'get',
    params: params
  })
}

export function getCatalogPermissions(params) {
  return request({
    url: '/cms/perms/catalog',
    method: 'get',
    params: params
  })
}