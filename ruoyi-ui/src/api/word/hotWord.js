import request from '@/utils/request'

export function getHotWordList(params) {
  return request({
    url: '/cms/hotword',
    method: 'get',
    params: params
  })
}

export function addHotWord(data) {
  return request({
    url: '/cms/hotword',
    method: 'post',
    data: data
  })
}

export function editHotWord(data) {
  return request({
    url: '/cms/hotword',
    method: 'put',
    data: data
  })
}

export function deleteHotWord(data) {
  return request({
    url: '/cms/hotword',
    method: 'delete',
    data: data
  })
}

export function getHotWordGroupTreeData() {
  return request({
    url: '/cms/hotwordgroup/treedata',
    method: 'get'
  })
}

export function getHotWordGroupOptions() {
  return request({
    url: '/cms/hotwordgroup/options',
    method: 'get'
  })
}

export function addHotWordGroup(data) {
  return request({
    url: '/cms/hotwordgroup',
    method: 'post',
    data: data
  })
}

export function editHotWordGroup(data) {
  return request({
    url: '/cms/hotwordgroup',
    method: 'put',
    data: data
  })
}

export function deleteHotWordGroup(data) {
  return request({
    url: '/cms/hotwordgroup',
    method: 'delete',
    data: data
  })
}