import request from '@/utils/request'
export default {
  listByParentId(parentId) {
    return request({
      url: `/admin/core/dict/getDictParentById/${parentId}`,
      method: 'get'
    })
  }
}