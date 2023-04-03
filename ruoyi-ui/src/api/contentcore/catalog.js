import request from '@/utils/request'

// 插叙栏目类型
export function getCatalogTypes() {
  return request({
    url: '/cms/catalog/getCatalogTypes',
    method: 'get',
  })
}

// 查询内容类型
export function getContentTypes() {
  return request({
    url: '/cms/catalog/getContentTypes',
    method: 'get',
  })
}

// 查询栏目树结构
export function getCatalogTreeData() {
  return request({
    url: '/cms/catalog/treeData',
    method: 'get'
  })
}

// 查询栏目信息
export function getCatalogData(catalogId) {
  return request({
    url: '/cms/catalog/' + catalogId,
    method: 'get'
  })
}

// 新增栏目
export function addCatalog(data) {
  return request({
    url: '/cms/catalog',
    method: 'post',
    data: data
  })
}

// 修改栏目
export function updateCatalog(data) {
  return request({
    url: '/cms/catalog',
    method: 'put',
    data: data
  })
}

// 删除栏目
export function delCatalog(catalogId) {
  return request({
    url: '/cms/catalog/' + catalogId,
    method: 'delete'
  })
}

// 转移栏目
export function moveCatalog(fromCatalogId, toCatalogId) {
  return request({
    url: '/cms/catalog/move/' + fromCatalogId + "/" + toCatalogId,
    method: 'post'
  })
}

// 发布栏目
export function publishCatalog(data) {
  return request({
    url: '/cms/catalog/publish',
    method: 'post',
    data: data
  })
}

// 获取栏目扩展属性
export function getCatalogExtends(params) {
  return request({
    url: '/cms/catalog/extends',
    method: 'get',
    params: params
  })
}

// 保存栏目扩展属性
export function saveCatalogExtends(catalogId, data) {
  return request({
    url: '/cms/catalog/extends/' + catalogId,
    method: 'put',
    data: data
  })
}

// 栏目属性应用到子栏目
export function applyToChildren(data) {
  return request({
    url: '/cms/catalog/apply_children',
    method: 'put',
    data: data
  })
}

// 修改栏目显示状态
export function changeVisible(catlaogId, visible) {
  return request({
    url: '/cms/catalog/visible',
    method: 'put',
    data: { catalogId: catlaogId, visible: visible }
  })
}
export function sortCatalog(data) {
  return request({
    url: '/cms/catalog/sort',
    method: 'put',
    data: data
  })
}
