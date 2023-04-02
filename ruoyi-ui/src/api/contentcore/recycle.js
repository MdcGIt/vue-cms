import request from '@/utils/request'

export function getRecycleContentList(query) {
  return request({
    url: '/cms/content/recycle',
    method: 'get',
    params: query
  })
}

export function recoverRecycleContent(data) {
  return request({
    url: '/cms/content/recycle/recover',
    method: 'post',
    data: data
  })
}