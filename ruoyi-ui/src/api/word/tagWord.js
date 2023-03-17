import request from '@/utils/request'

export function getTagWordList(params) {
  return request({
    url: '/cms/tagword',
    method: 'get',
    params: params
  })
}
export function addTagWord(data) {
  return request({
    url: '/cms/tagword',
    method: 'post',
    data: data
  })
}

export function editTagWord(data) {
  return request({
    url: '/cms/tagword',
    method: 'put',
    data: data
  })
}

export function deleteTagWord(data) {
  return request({
    url: '/cms/tagword',
    method: 'delete',
    data: data
  })
}

export function getTagWordGroupTreeData() {
  return request({
    url: '/cms/tagwordgroup/treedata',
    method: 'get'
  })
}

export function addTagWordGroup(data) {
  return request({
    url: '/cms/tagwordgroup',
    method: 'post',
    data: data
  })
}

export function editTagWordGroup(data) {
  return request({
    url: '/cms/tagwordgroup',
    method: 'put',
    data: data
  })
}

export function deleteTagWordGroup(data) {
  return request({
    url: '/cms/tagwordgroup',
    method: 'delete',
    data: data
  })
}