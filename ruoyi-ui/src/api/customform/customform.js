import request from '@/utils/request'

export function listCustomForms(params) {
  return request({
    url: '/cms/customform',
    method: 'get',
    params: params
  })
}

export function getCustomForm(formId) {
  return request({
    url: '/cms/customform/' + formId,
    method: 'get'
  })
}

export function addCustomForm(data) {
  return request({
    url: '/cms/customform',
    method: 'post',
    data: data
  })
}

export function editCustomForm(data) {
  return request({
    url: '/cms/customform',
    method: 'put',
    data: data
  })
}

export function deleteCustomForms(data) {
  return request({
    url: '/cms/customform',
    method: 'delete',
    data: data
  })
}