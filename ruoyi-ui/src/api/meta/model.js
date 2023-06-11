import request from '@/utils/request'

// 元数据字段控件列表
export function getControlOptions() {
  return request({
    url: '/xmodel/controls',
    method: 'get'
  })
}

// 查询扩展模型数据保存表
export function listModelDataTable() {
  return request({
    url: '/xmodel/tables',
    method: 'get'
  })
}

// 查询扩展模型数据保存表字段
export function listModelTableFields(params) {
  return request({
    url: '/xmodel/tableFields',
    method: 'get',
    params: params
  })
}

// 元数据模型列表
export function listModel(query) {
  return request({
    url: '/xmodel',
    method: 'get',
    params: query
  })
}

export function getModel(modelId) {
  return request({
    url: '/xmodel/' + modelId,
    method: 'get'
  })
}

// 查询元数据模型数据保存表字段
export function listTableFields(params) {
  return request({
    url: '/xmodel/tableFields',
    method: 'get',
    params: params
  })
}

// 新增元数据模型
export function addModel(data) {
  return request({
    url: '/xmodel',
    method: 'post',
    data: data
  })
}

// 修改元数据模型
export function editModel(data) {
  return request({
    url: '/xmodel',
    method: 'put',
    data: data
  })
}

// 删除元数据模型
export function deleteModel(data) {
  return request({
    url: '/xmodel',
    method: 'delete',
    data: data
  })
}

// 查询元数据模型字段列表
export function listModelField(query) {
  return request({
    url: '/xmodel/field',
    method: 'get',
    params: query
  })
}

// 新增元数据模型字段
export function addModelField(data) {
  return request({
    url: '/xmodel/field',
    method: 'post',
    data: data
  })
}

// 修改元数据模型字段
export function editModelField(data) {
  return request({
    url: '/xmodel/field',
    method: 'put',
    data: data
  })
}

// 删除元数据模型字段
export function deleteModelField(data) {
  return request({
    url: '/xmodel/field',
    method: 'delete',
    data: data
  })
}

// 获取元数据模型指定ID数据
export function getModelData(modelId, pkValue) {
  return request({
    url: '/xmodel/data/' + modelId + "?pkValue=" + pkValue,
    method: 'get'
  })
}