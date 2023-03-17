import request from '@/utils/request'

export function getSensitiveWordList(params) {
  return request({
    url: '/cms/sensitiveword',
    method: 'get',
    params: params
  })
}

export function addSensitiveWord(data) {
  return request({
    url: '/cms/sensitiveword',
    method: 'post',
    data: data
  })
}

export function editSensitiveWord(data) {
  return request({
    url: '/cms/sensitiveword',
    method: 'put',
    data: data
  })
}

export function deleteSensitiveWord(data) {
  return request({
    url: '/cms/sensitiveword',
    method: 'delete',
    data: data
  })
}