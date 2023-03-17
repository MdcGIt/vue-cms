import request from '@/utils/request'

// 异步任务信息集合
export function getAsyncTaskList(type) {
  return request({
    url: '/cms/asynctask/list?type=' + type,
    method: 'get'
  })
}

// 获取指定异步任务信息
export function getAsyncTaskInfo(taskId) {
  return request({
    url: '/cms/asynctask/info?taskId=' + taskId,
    method: 'get'
  })
}

// 中止指定异步任务
export function stopAsyncTask(taskIds) {
  return request({
    url: '/cms/asynctask/stop',
    method: 'post',
    data: taskIds
  })
}

// 删除异步任务
export function delAsyncTask(taskIds) {
  return request({
    url: '/cms/asynctask/delete',
    method: 'post',
    data: taskIds
  })
}