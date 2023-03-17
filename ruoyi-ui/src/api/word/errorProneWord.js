import request from '@/utils/request'

export function getErrorProneWordList(params) {
  return request({
    url: '/cms/errorproneword',
    method: 'get',
    params: params
  })
}

export function addErrorProneWord(data) {
  return request({
    url: '/cms/errorproneword',
    method: 'post',
    data: data
  })
}

export function editErrorProneWord(data) {
  return request({
    url: '/cms/errorproneword',
    method: 'put',
    data: data
  })
}

export function deleteErrorProneWord(data) {
  return request({
    url: '/cms/errorproneword',
    method: 'delete',
    data: data
  })
}