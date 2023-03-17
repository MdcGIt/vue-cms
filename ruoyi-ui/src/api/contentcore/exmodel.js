import request from '@/utils/request'

// 查询扩展模型列表
export function listXModel(query) {
  return request({
    url: '/cms/exmodel',
    method: 'get',
    params: query
  })
}

// 查询扩展模型数据保存表
export function listXModelDataTable() {
  return request({
    url: '/xmodel/tables',
    method: 'get'
  })
}

// 查询扩展模型数据保存表字段
export function listXModelTableFields(params) {
  return request({
    url: '/xmodel/tableFields',
    method: 'get',
    params: params
  })
}

// 新增扩展模型
export function addXModel(data) {
  return request({
    url: '/cms/exmodel',
    method: 'post',
    data: data
  })
}

// 修改扩展模型
export function editXMOdel(data) {
  return request({
    url: '/cms/exmodel',
    method: 'put',
    data: data
  })
}

// 删除扩展模型
export function deleteXModel(data) {
  return request({
    url: '/cms/exmodel',
    method: 'delete',
    data: data
  })
}

// 查询扩展模型字段列表
export function listXModelField(query) {
  return request({
    url: '/cms/exmodel/fields',
    method: 'get',
    params: query
  })
}

// 新增扩展模型字段
export function addXModelField(data) {
  return request({
    url: '/cms/exmodel/field',
    method: 'post',
    data: data
  })
}

// 修改扩展模型字段
export function editXModelField(data) {
  return request({
    url: '/cms/exmodel/field',
    method: 'put',
    data: data
  })
}

// 删除扩展模型字段
export function deleteXModelField(data) {
  return request({
    url: '/cms/exmodel/field',
    method: 'delete',
    data: data
  })
}

// 删除扩展模型字段
export function getXModelFieldData(modelId, pkValue) {
  return request({
    url: '/cms/exmodel/data/' + modelId + "?pkValue=" + pkValue,
    method: 'get'
  })
}